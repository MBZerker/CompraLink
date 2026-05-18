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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UpdateManager {
    private static final String UPDATE_URL =
            "https://raw.githubusercontent.com/MBZerker/CompraLink/main/update.json";

    private UpdateManager() {
    }

    public static void checkForUpdates(Activity activity, boolean manual) {
        new Thread(() -> {
            try {
                JSONObject info = readJson(UPDATE_URL);
                int versionCode = info.optInt("versionCode", 0);
                String versionName = info.optString("versionName", "");
                String apkUrl = info.optString("apkUrl", "");
                String notes = info.optString("notes", "");

                if (versionCode <= BuildConfig.VERSION_CODE || apkUrl.trim().isEmpty()) {
                    if (manual) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, "Seu app ja esta atualizado.", Toast.LENGTH_SHORT).show());
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

        new AlertDialog.Builder(activity)
                .setTitle("Atualizacao disponivel")
                .setMessage(message)
                .setPositiveButton("Atualizar", (dialog, which) -> downloadAndInstall(activity, apkUrl))
                .setNegativeButton("Depois", null)
                .show();
    }

    private static JSONObject readJson(String urlText) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlText).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("Accept", "application/json");
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

    private static void downloadAndInstall(Activity activity, String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
            Toast.makeText(activity, "Permita instalar updates do CompraLink e toque em Atualizar novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(activity, "Baixando atualizacao...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                File dir = new File(activity.getCacheDir(), "updates");
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IllegalStateException("Sem pasta de cache");
                }
                File apk = new File(dir, "CompraLink-update.apk");
                downloadFile(apkUrl, apk);
                activity.runOnUiThread(() -> openInstaller(activity, apk));
            } catch (Exception e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Falha ao baixar atualizacao.", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private static void downloadFile(String urlText, File target) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlText).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
             FileOutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static void openInstaller(Activity activity, File apk) {
        Uri uri = FileProvider.getUriForFile(
                activity,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                apk
        );
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
