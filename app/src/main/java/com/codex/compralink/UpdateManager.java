package com.codex.compralink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UpdateManager {
    private static final String UPDATE_URL =
            "https://raw.githubusercontent.com/MBZerker/CompraLink/main/update.json";
    private static String pendingInstallUrl;

    private UpdateManager() {
    }

    public static void checkForUpdates(Activity activity, boolean manual) {
        new Thread(() -> {
            try {
                JSONObject info = readJson(UPDATE_URL);
                int versionCode = info.optInt("versionCode", 0);
                String versionName = info.optString("versionName", "");
                String notes = info.optString("notes", "");
                String releaseUrl = info.optString("releaseUrl", "https://github.com/MBZerker/CompraLink");
                String apkUrl = info.optString("apkUrl", releaseUrl);

                if (versionCode <= BuildConfig.VERSION_CODE) {
                    if (manual) {
                        activity.runOnUiThread(() -> showCurrentDialog(activity, apkUrl));
                    }
                    return;
                }

                activity.runOnUiThread(() -> showUpdateDialog(activity, versionName, notes, apkUrl));
            } catch (Exception e) {
                if (manual) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, "Nao foi possivel verificar atualizacao.", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private static void showUpdateDialog(Activity activity, String versionName, String notes, String apkUrl) {
        String message = "Existe uma nova versao";
        if (!versionName.isEmpty()) message += " (" + versionName + ")";
        if (!notes.isEmpty()) message += ".\n\n" + notes;
        message += "\n\nToque em BAIXAR E INSTALAR. O app vai baixar o APK e abrir a instalacao do Android.";

        new AlertDialog.Builder(activity)
                .setTitle("Atualizacao disponivel")
                .setMessage(message)
                .setPositiveButton("Baixar e instalar", (dialog, which) -> downloadAndInstall(activity, apkUrl))
                .setNegativeButton("Depois", null)
                .show();
    }

    private static void showCurrentDialog(Activity activity, String apkUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("App atualizado")
                .setMessage("Esta versao ja esta atualizada. Se quiser reinstalar o APK publicado, toque em baixar.")
                .setPositiveButton("Baixar APK", (dialog, which) -> downloadAndInstall(activity, apkUrl))
                .setNegativeButton("Fechar", null)
                .show();
    }

    public static void resumePendingInstall(Activity activity) {
        if (pendingInstallUrl == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            return;
        }
        String url = pendingInstallUrl;
        pendingInstallUrl = null;
        downloadAndInstall(activity, url);
    }

    private static void downloadAndInstall(Activity activity, String apkUrl) {
        if (!isPublicHttpUrl(apkUrl)) {
            Toast.makeText(activity, "Link de atualizacao invalido.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            pendingInstallUrl = apkUrl;
            showInstallPermissionDialog(activity);
            return;
        }

        Toast.makeText(activity, "Baixando atualizacao...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                File apkFile = downloadApk(activity, apkUrl);
                activity.runOnUiThread(() -> installApk(activity, apkFile));
            } catch (Exception e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Nao foi possivel baixar a atualizacao.", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private static void showInstallPermissionDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Permissao necessaria")
                .setMessage("O Android precisa permitir que o Check Mercado instale atualizacoes baixadas pelo proprio app. Ative a permissao e volte para continuar.")
                .setPositiveButton("Abrir permissao", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> pendingInstallUrl = null)
                .show();
    }

    private static File downloadApk(Activity activity, String apkUrl) throws Exception {
        File updateDir = new File(activity.getCacheDir(), "updates");
        if (!updateDir.exists() && !updateDir.mkdirs()) {
            throw new IllegalStateException("Nao foi possivel criar pasta de atualizacao.");
        }
        File apkFile = new File(updateDir, "CheckMercado-update.apk");
        if (apkFile.exists() && !apkFile.delete()) {
            throw new IllegalStateException("Nao foi possivel trocar APK antigo.");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(apkUrl).openConnection();
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Accept", "application/vnd.android.package-archive,*/*");
        connection.setRequestProperty("User-Agent", "CheckMercado/" + BuildConfig.VERSION_NAME);
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            connection.disconnect();
            throw new IllegalStateException("HTTP " + status);
        }
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(apkFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            connection.disconnect();
        }
        if (apkFile.length() < 1024 * 1024) {
            throw new IllegalStateException("APK baixado parece invalido.");
        }
        return apkFile;
    }

    private static void installApk(Activity activity, File apkFile) {
        Uri apkUri = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".fileprovider", apkFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Nao foi possivel abrir o instalador do Android.", Toast.LENGTH_LONG).show();
        }
    }

    private static boolean isPublicHttpUrl(String url) {
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
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
}
