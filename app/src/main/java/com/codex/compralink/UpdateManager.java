package com.codex.compralink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class UpdateManager {
    private static final String UPDATE_URL =
            "https://raw.githubusercontent.com/MBZerker/CompraLink/main/update.json";
    private static final String PREFS = "compralink_update_state";
    private static final String KEY_VERSION_CODE = "pending_update_version_code";
    private static final String KEY_VERSION_NAME = "pending_update_version_name";
    private static final String KEY_APK_URL = "pending_update_apk_url";
    private static final String KEY_FILE_PATH = "pending_update_file_path";
    private static final String KEY_DOWNLOAD_COMPLETE = "pending_update_download_complete";
    private static final String KEY_BYTES_DOWNLOADED = "pending_update_bytes_downloaded";
    private static final String KEY_TOTAL_BYTES = "pending_update_total_bytes";
    private static final String KEY_WAITING_PERMISSION = "pending_update_waiting_permission";
    private static final String KEY_LAST_ERROR = "pending_update_last_error";
    private static final String KEY_STARTED_AT = "pending_update_started_at";
    private static final String KEY_SHA256 = "pending_update_sha256";
    private static final String KEY_SIZE_BYTES = "pending_update_size_bytes";

    private static AlertDialog updateDialog;
    private static DownloadOverlay downloadOverlay;
    private static volatile boolean cancelRequested;
    private static volatile boolean downloadRunning;
    private static UpdateState activeDownloadState;

    private UpdateManager() {
    }

    public static void checkForUpdates(Activity activity, boolean manual) {
        new Thread(() -> {
            try {
                JSONObject info = readJson(UPDATE_URL);
                UpdateState remote = stateFromUpdateJson(activity, info);
                if (remote.versionCode <= BuildConfig.VERSION_CODE) {
                    if (manual) {
                        activity.runOnUiThread(() -> showCurrentDialog(activity));
                    }
                    return;
                }

                activity.runOnUiThread(() -> {
                    UpdateState pending = loadPendingUpdateState(activity);
                    if (sameRemoteUpdate(pending, remote) && pending.downloadComplete) {
                        File apkFile = pending.file();
                        ApkValidationResult validation = validateDownloadedApk(activity, apkFile, pending);
                        if (validation.ok) {
                            showDownloadedDialog(activity, pending);
                            return;
                        }
                        invalidatePendingFile(activity, pending, validation.message);
                    }
                    showUpdateDialog(activity, remote);
                });
            } catch (Exception e) {
                if (manual) {
                    activity.runOnUiThread(() ->
                            showErrorDialog(activity, null, "N\u00e3o foi poss\u00edvel verificar atualiza\u00e7\u00e3o.", false));
                }
            }
        }).start();
    }

    public static void resumePendingInstall(Activity activity) {
        UpdateState state = loadPendingUpdateState(activity);
        if (state == null) return;
        if (downloadRunning) {
            if (downloadOverlay == null) {
                downloadOverlay = new DownloadOverlay(activity, () -> cancelDownload(activity, state));
            }
            downloadOverlay.show(state);
            return;
        }
        if (state.downloadComplete) {
            ApkValidationResult validation = validateDownloadedApk(activity, state.file(), state);
            if (!validation.ok) {
                invalidatePendingFile(activity, state, validation.message);
                showErrorDialog(activity, state, validation.message, true);
                return;
            }
            if (needsInstallPermission(activity)) {
                state.waitingPermission = true;
                state.lastError = "";
                savePendingUpdateState(activity, state);
                showInstallPermissionDialog(activity, state);
                return;
            }
            showDownloadedDialog(activity, state);
            return;
        }
        if (state.waitingPermission) {
            showInstallPermissionDialog(activity, state);
            return;
        }
        if (!isBlank(state.lastError)) {
            showErrorDialog(activity, state, state.lastError, true);
            return;
        }
        if (state.bytesDownloaded > 0) {
            state.lastError = "Download interrompido antes de concluir.";
            savePendingUpdateState(activity, state);
            showErrorDialog(activity, state, state.lastError, true);
        }
    }

    private static void showUpdateDialog(Activity activity, UpdateState state) {
        dismissUpdateDialog();
        StringBuilder message = new StringBuilder("Existe uma nova vers\u00e3o");
        if (!isBlank(state.versionName)) message.append(" (").append(state.versionName).append(")");
        if (!isBlank(state.notes)) message.append(".\n\n").append(state.notes);
        message.append("\n\nToque em Baixar e instalar. O app vai baixar o APK e abrir a instala\u00e7\u00e3o do Android.");

        updateDialog = new AlertDialog.Builder(activity)
                .setTitle("Atualiza\u00e7\u00e3o dispon\u00edvel")
                .setMessage(message.toString())
                .setPositiveButton("Baixar e instalar", (dialog, which) -> startOrResumeDownload(activity, state))
                .setNegativeButton("Depois", null)
                .show();
    }

    private static void showCurrentDialog(Activity activity) {
        dismissUpdateDialog();
        updateDialog = new AlertDialog.Builder(activity)
                .setTitle("App atualizado")
                .setMessage("Esta vers\u00e3o j\u00e1 est\u00e1 atualizada.")
                .setPositiveButton("Fechar", null)
                .show();
    }

    private static void showDownloadedDialog(Activity activity, UpdateState state) {
        dismissUpdateDialog();
        updateDialog = new AlertDialog.Builder(activity)
                .setTitle("Atualiza\u00e7\u00e3o baixada")
                .setMessage("O APK j\u00e1 foi baixado. Continue a instala\u00e7\u00e3o.")
                .setPositiveButton("Instalar agora", (dialog, which) -> installApk(activity, state))
                .setNeutralButton("Baixar novamente", (dialog, which) -> {
                    deleteFileQuietly(state.file());
                    state.downloadComplete = false;
                    state.bytesDownloaded = 0;
                    state.totalBytes = 0;
                    state.lastError = "";
                    state.waitingPermission = false;
                    savePendingUpdateState(activity, state);
                    startOrResumeDownload(activity, state);
                })
                .setNegativeButton("Limpar arquivo", (dialog, which) -> {
                    deleteFileQuietly(state.file());
                    clearPendingUpdateState(activity);
                })
                .show();
    }

    private static void showInstallPermissionDialog(Activity activity, UpdateState state) {
        dismissUpdateDialog();
        state.waitingPermission = true;
        savePendingUpdateState(activity, state);
        updateDialog = new AlertDialog.Builder(activity)
                .setTitle("Permiss\u00e3o necess\u00e1ria")
                .setMessage("Permita que o Check Mercado instale atualiza\u00e7\u00f5es. Ao voltar, a instala\u00e7\u00e3o continuar\u00e1.")
                .setPositiveButton("Abrir permiss\u00e3o", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        updateDialog.setCanceledOnTouchOutside(false);
    }

    private static void showErrorDialog(Activity activity, UpdateState state, String error, boolean canRetry) {
        dismissUpdateDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("Erro na atualiza\u00e7\u00e3o")
                .setMessage(error)
                .setNegativeButton("Fechar", null);
        if (canRetry && state != null && isPublicHttpUrl(state.apkUrl)) {
            builder.setPositiveButton("Tentar novamente", (dialog, which) -> {
                state.lastError = "";
                state.downloadComplete = false;
                state.waitingPermission = false;
                state.bytesDownloaded = 0;
                state.totalBytes = 0;
                deleteFileQuietly(state.file());
                savePendingUpdateState(activity, state);
                startOrResumeDownload(activity, state);
            });
        }
        updateDialog = builder.show();
    }

    private static void showSignatureMismatchDialog(Activity activity, UpdateState state, String message) {
        dismissUpdateDialog();
        updateDialog = new AlertDialog.Builder(activity)
                .setTitle("Assinatura diferente")
                .setMessage(message + "\n\nFa\u00e7a backup antes de desinstalar qualquer vers\u00e3o.")
                .setPositiveButton("Fazer backup", (dialog, which) -> {
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).openBackupFromUpdater();
                    } else {
                        Toast.makeText(activity, "Abra o menu principal e use Backup antes de desinstalar.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNeutralButton("Limpar atualiza\u00e7\u00e3o baixada", (dialog, which) -> {
                    deleteFileQuietly(state.file());
                    clearPendingUpdateState(activity);
                })
                .setNegativeButton("Entendi", null)
                .show();
    }

    private static void startOrResumeDownload(Activity activity, UpdateState state) {
        if (!isPublicHttpUrl(state.apkUrl)) {
            showErrorDialog(activity, state, "Link de atualiza\u00e7\u00e3o inv\u00e1lido.", false);
            return;
        }

        UpdateState pending = loadPendingUpdateState(activity);
        if (sameRemoteUpdate(pending, state) && pending.downloadComplete) {
            ApkValidationResult validation = validateDownloadedApk(activity, pending.file(), pending);
            if (validation.ok) {
                showDownloadedDialog(activity, pending);
                return;
            }
            invalidatePendingFile(activity, pending, validation.message);
        }

        if (downloadRunning) {
            if (downloadOverlay != null) downloadOverlay.show(state);
            return;
        }

        state.filePath = pendingUpdateFileFor(activity, state).getAbsolutePath();
        state.downloadComplete = false;
        state.waitingPermission = false;
        state.lastError = "";
        state.bytesDownloaded = 0;
        state.totalBytes = Math.max(0, state.sizeBytes);
        state.startedAt = System.currentTimeMillis();
        savePendingUpdateState(activity, state);

        dismissUpdateDialog();
        cancelRequested = false;
        downloadRunning = true;
        activeDownloadState = state;
        downloadOverlay = new DownloadOverlay(activity, () -> cancelDownload(activity, state));
        downloadOverlay.show(state);

        new Thread(() -> downloadApk(activity, state)).start();
    }

    private static void downloadApk(Activity activity, UpdateState state) {
        File apkFile = state.file();
        HttpURLConnection connection = null;
        long lastUi = 0;
        long lastPersist = 0;
        try {
            File parent = apkFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("N\u00e3o foi poss\u00edvel criar pasta de atualiza\u00e7\u00e3o.");
            }
            deleteFileQuietly(apkFile);

            connection = (HttpURLConnection) new URL(state.apkUrl).openConnection();
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Accept", "application/vnd.android.package-archive,*/*");
            connection.setRequestProperty("User-Agent", "CheckMercado/" + BuildConfig.VERSION_NAME);
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("HTTP " + status);
            }

            long contentLength = contentLength(connection);
            if (state.sizeBytes > 0) contentLength = state.sizeBytes;
            state.totalBytes = Math.max(0, contentLength);
            savePendingUpdateState(activity, state);
            updateOverlay(activity, state);

            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(apkFile)) {
                byte[] buffer = new byte[16384];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (cancelRequested) throw new DownloadCanceledException();
                    output.write(buffer, 0, read);
                    state.bytesDownloaded += read;
                    long now = System.currentTimeMillis();
                    if (now - lastUi > 250) {
                        lastUi = now;
                        updateOverlay(activity, state);
                    }
                    if (now - lastPersist > 700) {
                        lastPersist = now;
                        savePendingUpdateState(activity, state);
                    }
                }
                output.flush();
            }

            state.bytesDownloaded = apkFile.length();
            state.downloadComplete = true;
            state.waitingPermission = false;
            state.lastError = "";
            savePendingUpdateState(activity, state);

            ApkValidationResult validation = validateDownloadedApk(activity, apkFile, state);
            if (!validation.ok) {
                if (validation.signatureMismatch) {
                    activity.runOnUiThread(() -> {
                        dismissDownloadOverlay();
                        showSignatureMismatchDialog(activity, state, validation.message);
                    });
                    return;
                }
                invalidatePendingFile(activity, state, validation.message);
                activity.runOnUiThread(() -> {
                    dismissDownloadOverlay();
                    showErrorDialog(activity, state, validation.message, true);
                });
                return;
            }

            activity.runOnUiThread(() -> {
                dismissDownloadOverlay();
                showDownloadedDialog(activity, state);
            });
        } catch (DownloadCanceledException e) {
            deleteFileQuietly(apkFile);
            clearPendingUpdateState(activity);
            activity.runOnUiThread(() -> {
                dismissDownloadOverlay();
                Toast.makeText(activity, "Download cancelado.", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            state.downloadComplete = false;
            state.waitingPermission = false;
            state.lastError = "N\u00e3o foi poss\u00edvel baixar a atualiza\u00e7\u00e3o.\n\n" + readableError(e);
            savePendingUpdateState(activity, state);
            activity.runOnUiThread(() -> {
                dismissDownloadOverlay();
                showErrorDialog(activity, state, state.lastError, true);
            });
        } finally {
            if (connection != null) connection.disconnect();
            downloadRunning = false;
            activeDownloadState = null;
            cancelRequested = false;
        }
    }

    private static void installApk(Activity activity, UpdateState state) {
        ApkValidationResult validation = validateDownloadedApk(activity, state.file(), state);
        if (!validation.ok) {
            if (validation.signatureMismatch) {
                showSignatureMismatchDialog(activity, state, validation.message);
            } else {
                invalidatePendingFile(activity, state, validation.message);
                showErrorDialog(activity, state, validation.message, true);
            }
            return;
        }
        if (needsInstallPermission(activity)) {
            showInstallPermissionDialog(activity, state);
            return;
        }

        Uri apkUri = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".fileprovider", state.file());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setClipData(ClipData.newUri(activity.getContentResolver(), "Check Mercado update", apkUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(intent);
            clearPendingUpdateState(activity);
        } catch (Exception e) {
            state.lastError = "N\u00e3o foi poss\u00edvel abrir o instalador do Android.";
            savePendingUpdateState(activity, state);
            showErrorDialog(activity, state, state.lastError, false);
        }
    }

    private static ApkValidationResult validateDownloadedApk(Activity activity, File apkFile, UpdateState state) {
        if (apkFile == null || !apkFile.exists()) {
            return ApkValidationResult.error("Arquivo de atualiza\u00e7\u00e3o n\u00e3o encontrado.");
        }
        if (apkFile.length() < 1024 * 1024) {
            return ApkValidationResult.error("Arquivo de atualiza\u00e7\u00e3o inv\u00e1lido. Baixe novamente.");
        }
        if (state.sizeBytes > 0 && apkFile.length() != state.sizeBytes) {
            return ApkValidationResult.error("Tamanho do APK diferente do esperado. Baixe novamente.");
        }
        if (state.totalBytes > 0 && apkFile.length() != state.totalBytes) {
            return ApkValidationResult.error("Download incompleto. Baixe novamente.");
        }
        if (!hasZipHeader(apkFile)) {
            return ApkValidationResult.error("Arquivo baixado n\u00e3o parece ser um APK v\u00e1lido.");
        }
        if (!isBlank(state.sha256)) {
            String actual = sha256Hex(apkFile);
            if (!state.sha256.equalsIgnoreCase(actual)) {
                return ApkValidationResult.error("SHA-256 do APK n\u00e3o confere. Baixe novamente.");
            }
        }

        PackageManager pm = activity.getPackageManager();
        PackageInfo archiveInfo = getArchivePackageInfo(pm, apkFile.getAbsolutePath());
        if (archiveInfo == null) {
            return ApkValidationResult.error("Arquivo de atualiza\u00e7\u00e3o inv\u00e1lido. Baixe novamente.");
        }
        if (!activity.getPackageName().equals(archiveInfo.packageName)) {
            return ApkValidationResult.error("O APK baixado pertence a outro app.");
        }

        long apkVersionCode = versionCodeOf(archiveInfo);
        if (apkVersionCode <= BuildConfig.VERSION_CODE) {
            return ApkValidationResult.error("O APK baixado n\u00e3o \u00e9 mais novo que a vers\u00e3o instalada.");
        }
        if (state.versionCode > 0 && apkVersionCode != state.versionCode) {
            return ApkValidationResult.error("A vers\u00e3o do APK n\u00e3o corresponde ao update.json.");
        }
        int minSdk = minSdkOf(archiveInfo);
        if (minSdk > 0 && Build.VERSION.SDK_INT < minSdk) {
            return ApkValidationResult.error("Este APK exige uma vers\u00e3o mais nova do Android.");
        }

        SignatureCheck signatureCheck = signaturesMatch(activity, archiveInfo);
        if (signatureCheck.checked && !signatureCheck.match) {
            return ApkValidationResult.signatureMismatch("Esta atualiza\u00e7\u00e3o foi assinada com uma chave diferente da vers\u00e3o instalada.");
        }
        return ApkValidationResult.ok();
    }

    private static void cancelDownload(Activity activity, UpdateState state) {
        cancelRequested = true;
        state.lastError = "Download cancelado.";
        savePendingUpdateState(activity, state);
    }

    private static void updateOverlay(Activity activity, UpdateState state) {
        activity.runOnUiThread(() -> {
            if (downloadOverlay != null) downloadOverlay.update(state);
        });
    }

    private static void dismissUpdateDialog() {
        if (updateDialog != null && updateDialog.isShowing()) updateDialog.dismiss();
        updateDialog = null;
    }

    private static void dismissDownloadOverlay() {
        if (downloadOverlay != null) downloadOverlay.dismiss();
        downloadOverlay = null;
    }

    private static void invalidatePendingFile(Activity activity, UpdateState state, String error) {
        deleteFileQuietly(state.file());
        state.downloadComplete = false;
        state.waitingPermission = false;
        state.bytesDownloaded = 0;
        state.totalBytes = 0;
        state.lastError = error;
        savePendingUpdateState(activity, state);
    }

    private static UpdateState stateFromUpdateJson(Activity activity, JSONObject info) {
        UpdateState state = new UpdateState();
        state.versionCode = info.optInt("versionCode", 0);
        state.versionName = info.optString("versionName", "");
        state.notes = info.optString("notes", "");
        String releaseUrl = info.optString("releaseUrl", "https://github.com/MBZerker/CompraLink");
        state.apkUrl = info.optString("apkUrl", releaseUrl);
        state.sha256 = info.optString("sha256", "");
        state.sizeBytes = info.optLong("sizeBytes", 0);
        state.filePath = pendingUpdateFileFor(activity, state).getAbsolutePath();
        return state;
    }

    private static boolean sameRemoteUpdate(UpdateState a, UpdateState b) {
        return a != null && b != null
                && a.versionCode == b.versionCode
                && safeEquals(a.apkUrl, b.apkUrl);
    }

    private static File pendingUpdateFileFor(Activity activity, UpdateState state) {
        File updateDir = new File(activity.getCacheDir(), "updates");
        String suffix = state.versionCode > 0 ? String.valueOf(state.versionCode) : "latest";
        return new File(updateDir, "CheckMercado-update-" + suffix + ".apk");
    }

    private static void savePendingUpdateState(Context context, UpdateState state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_VERSION_CODE, state.versionCode);
        editor.putString(KEY_VERSION_NAME, state.versionName);
        editor.putString(KEY_APK_URL, state.apkUrl);
        editor.putString(KEY_FILE_PATH, state.filePath);
        editor.putBoolean(KEY_DOWNLOAD_COMPLETE, state.downloadComplete);
        editor.putLong(KEY_BYTES_DOWNLOADED, state.bytesDownloaded);
        editor.putLong(KEY_TOTAL_BYTES, state.totalBytes);
        editor.putBoolean(KEY_WAITING_PERMISSION, state.waitingPermission);
        editor.putString(KEY_LAST_ERROR, state.lastError);
        editor.putLong(KEY_STARTED_AT, state.startedAt);
        editor.putString(KEY_SHA256, state.sha256);
        editor.putLong(KEY_SIZE_BYTES, state.sizeBytes);
        editor.apply();
    }

    private static UpdateState loadPendingUpdateState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int versionCode = prefs.getInt(KEY_VERSION_CODE, 0);
        String apkUrl = prefs.getString(KEY_APK_URL, "");
        String filePath = prefs.getString(KEY_FILE_PATH, "");
        if (versionCode <= 0 && isBlank(apkUrl) && isBlank(filePath)) return null;
        UpdateState state = new UpdateState();
        state.versionCode = versionCode;
        state.versionName = prefs.getString(KEY_VERSION_NAME, "");
        state.apkUrl = apkUrl;
        state.filePath = filePath;
        state.downloadComplete = prefs.getBoolean(KEY_DOWNLOAD_COMPLETE, false);
        state.bytesDownloaded = prefs.getLong(KEY_BYTES_DOWNLOADED, 0);
        state.totalBytes = prefs.getLong(KEY_TOTAL_BYTES, 0);
        state.waitingPermission = prefs.getBoolean(KEY_WAITING_PERMISSION, false);
        state.lastError = prefs.getString(KEY_LAST_ERROR, "");
        state.startedAt = prefs.getLong(KEY_STARTED_AT, 0);
        state.sha256 = prefs.getString(KEY_SHA256, "");
        state.sizeBytes = prefs.getLong(KEY_SIZE_BYTES, 0);
        return state;
    }

    private static void clearPendingUpdateState(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    private static boolean needsInstallPermission(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !activity.getPackageManager().canRequestPackageInstalls();
    }

    private static JSONObject readJson(String urlText) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlText).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("Accept", "application/json");
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            connection.disconnect();
            throw new IllegalStateException("HTTP " + status);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return new JSONObject(builder.toString());
        } finally {
            connection.disconnect();
        }
    }

    private static long contentLength(HttpURLConnection connection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Math.max(0, connection.getContentLengthLong());
        }
        return Math.max(0, connection.getContentLength());
    }

    private static PackageInfo getArchivePackageInfo(PackageManager pm, String path) {
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                ? PackageManager.GET_SIGNING_CERTIFICATES
                : PackageManager.GET_SIGNATURES;
        return pm.getPackageArchiveInfo(path, flags);
    }

    private static long versionCodeOf(PackageInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return info.getLongVersionCode();
        }
        return info.versionCode;
    }

    private static int minSdkOf(PackageInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && info.applicationInfo != null) {
            return info.applicationInfo.minSdkVersion;
        }
        return 0;
    }

    private static SignatureCheck signaturesMatch(Activity activity, PackageInfo archiveInfo) {
        try {
            PackageManager pm = activity.getPackageManager();
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? PackageManager.GET_SIGNING_CERTIFICATES
                    : PackageManager.GET_SIGNATURES;
            PackageInfo installedInfo = pm.getPackageInfo(activity.getPackageName(), flags);
            Set<String> installed = signatureDigests(installedInfo);
            Set<String> archive = signatureDigests(archiveInfo);
            if (installed.isEmpty() || archive.isEmpty()) return SignatureCheck.notChecked();
            for (String value : archive) {
                if (installed.contains(value)) return new SignatureCheck(true, true);
            }
            return new SignatureCheck(true, false);
        } catch (Exception e) {
            return SignatureCheck.notChecked();
        }
    }

    private static Set<String> signatureDigests(PackageInfo info) throws Exception {
        Set<String> values = new HashSet<>();
        Signature[] signatures = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && info.signingInfo != null) {
            signatures = info.signingInfo.hasMultipleSigners()
                    ? info.signingInfo.getApkContentsSigners()
                    : info.signingInfo.getSigningCertificateHistory();
        }
        if ((signatures == null || signatures.length == 0) && info.signatures != null) {
            signatures = info.signatures;
        }
        if (signatures == null) return values;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Signature signature : signatures) {
            values.add(toHex(digest.digest(signature.toByteArray())));
        }
        return values;
    }

    private static boolean hasZipHeader(File file) {
        try (FileInputStream input = new FileInputStream(file)) {
            return input.read() == 'P' && input.read() == 'K';
        } catch (Exception e) {
            return false;
        }
    }

    private static String sha256Hex(File file) {
        try (FileInputStream input = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[16384];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return toHex(digest.digest());
        } catch (Exception e) {
            return "";
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format(Locale.US, "%02x", value & 0xff));
        }
        return builder.toString();
    }

    private static void deleteFileQuietly(File file) {
        if (file != null && file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private static String readableError(Exception e) {
        String message = e.getMessage();
        if (isBlank(message)) return e.getClass().getSimpleName();
        return e.getClass().getSimpleName() + ": " + message;
    }

    private static boolean isPublicHttpUrl(String url) {
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String formatBytes(long bytes) {
        if (bytes >= 1024L * 1024L) {
            return String.format(Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0);
        }
        if (bytes >= 1024L) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }

    private static final class DownloadOverlay {
        private final AlertDialog dialog;
        private final ProgressBar progressBar;
        private final TextView percent;
        private final TextView detail;

        DownloadOverlay(Activity activity, Runnable cancelAction) {
            LinearLayout content = new LinearLayout(activity);
            content.setOrientation(LinearLayout.VERTICAL);
            int padding = dp(activity, 18);
            content.setPadding(padding, padding, padding, padding);

            TextView message = new TextView(activity);
            message.setText("Aguarde. N\u00e3o feche esta tela.");
            message.setTextSize(15);
            content.addView(message, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 18));
            progressParams.setMargins(0, dp(activity, 14), 0, 0);
            content.addView(progressBar, progressParams);

            percent = new TextView(activity);
            percent.setTextSize(18);
            percent.setPadding(0, dp(activity, 12), 0, 0);
            content.addView(percent);

            detail = new TextView(activity);
            detail.setTextSize(13);
            detail.setPadding(0, dp(activity, 6), 0, 0);
            content.addView(detail);

            dialog = new AlertDialog.Builder(activity)
                    .setTitle("Baixando atualiza\u00e7\u00e3o")
                    .setView(content)
                    .setNegativeButton("Cancelar download", (d, which) -> cancelAction.run())
                    .create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        void show(UpdateState state) {
            if (!dialog.isShowing()) dialog.show();
            update(state);
        }

        void update(UpdateState state) {
            long total = state.totalBytes;
            long downloaded = state.bytesDownloaded;
            if (total > 0) {
                int value = (int) Math.max(0, Math.min(100, downloaded * 100 / total));
                progressBar.setIndeterminate(false);
                progressBar.setProgress(value);
                percent.setText(value + "%");
                detail.setText(formatBytes(downloaded) + " de " + formatBytes(total));
            } else {
                progressBar.setIndeterminate(true);
                percent.setText("Baixando...");
                detail.setText(formatBytes(downloaded) + " baixados");
            }
        }

        void dismiss() {
            if (dialog.isShowing()) dialog.dismiss();
        }
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static final class UpdateState {
        int versionCode;
        String versionName = "";
        String notes = "";
        String apkUrl = "";
        String filePath = "";
        boolean downloadComplete;
        long bytesDownloaded;
        long totalBytes;
        boolean waitingPermission;
        String lastError = "";
        long startedAt;
        String sha256 = "";
        long sizeBytes;

        File file() {
            return isBlank(filePath) ? null : new File(filePath);
        }
    }

    private static final class ApkValidationResult {
        final boolean ok;
        final String message;
        final boolean signatureMismatch;

        private ApkValidationResult(boolean ok, String message, boolean signatureMismatch) {
            this.ok = ok;
            this.message = message;
            this.signatureMismatch = signatureMismatch;
        }

        static ApkValidationResult ok() {
            return new ApkValidationResult(true, "", false);
        }

        static ApkValidationResult error(String message) {
            return new ApkValidationResult(false, message, false);
        }

        static ApkValidationResult signatureMismatch(String message) {
            return new ApkValidationResult(false, message, true);
        }
    }

    private static final class SignatureCheck {
        final boolean checked;
        final boolean match;

        private SignatureCheck(boolean checked, boolean match) {
            this.checked = checked;
            this.match = match;
        }

        static SignatureCheck notChecked() {
            return new SignatureCheck(false, false);
        }
    }

    private static final class DownloadCanceledException extends Exception {
    }
}
