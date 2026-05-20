package com.codex.compralink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
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
        message += "\n\nToque em BAIXAR ATUALIZACAO para abrir o APK. Depois confirme o download e a instalacao no Android.";

        new AlertDialog.Builder(activity)
                .setTitle("Atualizacao disponivel")
                .setMessage(message)
                .setPositiveButton("Baixar atualizacao", (dialog, which) -> openUrl(activity, apkUrl))
                .setNegativeButton("Depois", null)
                .show();
    }

    private static void showCurrentDialog(Activity activity, String apkUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("App atualizado")
                .setMessage("Esta versao ja esta atualizada. Se quiser reinstalar o APK publicado, toque em baixar.")
                .setPositiveButton("Baixar APK", (dialog, which) -> openUrl(activity, apkUrl))
                .setNegativeButton("Fechar", null)
                .show();
    }

    private static void openUrl(Activity activity, String url) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
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
}
