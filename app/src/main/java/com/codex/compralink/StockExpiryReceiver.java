package com.codex.compralink;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StockExpiryReceiver extends BroadcastReceiver {
    private static final String PREFS = "compralink";
    private static final String KEY_STOCK = "stock";
    private static final String KEY_ENABLED = "stock_notifications_enabled";
    private static final String KEY_DAYS = "stock_expiry_notice_days";
    private static final String KEY_LAST_NOTICE = "stock_expiry_last_notice";
    private static final String ACTION_CHECK = "com.codex.compralink.STOCK_EXPIRY_CHECK";
    private static final String CHANNEL_ID = "stock_expiry";
    private static final int REQUEST_ALARM = 41026;
    private static final int NOTIFICATION_ID = 41027;

    @Override
    public void onReceive(Context context, Intent intent) {
        checkAndNotify(context, false);
        schedule(context);
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Validade da despensa", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Avisos de itens pr\u00f3ximos da validade na despensa.");
        channel.enableLights(true);
        channel.setLightColor(Color.rgb(22, 163, 74));
        manager.createNotificationChannel(channel);
    }

    static void schedule(Context context) {
        if (!context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)) {
            cancel(context);
            return;
        }
        ensureChannel(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        PendingIntent pendingIntent = pendingIntent(context);
        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextCheckAt(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.cancel(pendingIntent(context));
    }

    static int checkAndNotify(Context context, boolean force) {
        if (!context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false) && !force) return 0;
        int noticeDays = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_DAYS, 7);
        List<ExpiryRow> rows = dueRows(context, noticeDays);
        if (rows.isEmpty()) return 0;
        if (!force && alreadyNotifiedToday(context, rows)) return rows.size();
        if (!canPostNotifications(context)) return rows.size();
        ensureChannel(context);
        postNotification(context, rows);
        rememberNotifiedToday(context, rows);
        return rows.size();
    }

    private static boolean canPostNotifications(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private static List<ExpiryRow> dueRows(Context context, int noticeDays) {
        List<ExpiryRow> rows = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_STOCK, "[]");
        long today = startOfDay(System.currentTimeMillis());
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                long expiryAt = item.optLong("expiryAt", 0);
                if (expiryAt <= 0) continue;
                long days = (startOfDay(expiryAt) - today) / 86400000L;
                if (days <= noticeDays) {
                    rows.add(new ExpiryRow(item.optString("id", String.valueOf(i)), item.optString("name", "Item"), days));
                }
            }
        } catch (Exception ignored) {
        }
        return rows;
    }

    private static void postNotification(Context context, List<ExpiryRow> rows) {
        String title = rows.size() == 1 ? "Item pr\u00f3ximo da validade" : rows.size() + " itens pr\u00f3ximos da validade";
        String body = buildBody(rows);
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification_cart)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) builder.setPriority(Notification.PRIORITY_DEFAULT);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(NOTIFICATION_ID, builder.build());
    }

    private static String buildBody(List<ExpiryRow> rows) {
        StringBuilder body = new StringBuilder();
        int limit = Math.min(rows.size(), 5);
        for (int i = 0; i < limit; i++) {
            if (body.length() > 0) body.append('\n');
            ExpiryRow row = rows.get(i);
            body.append(row.name).append(": ").append(daysText(row.days));
        }
        if (rows.size() > limit) body.append('\n').append("+ ").append(rows.size() - limit).append(" item(ns)");
        return body.toString();
    }

    private static String daysText(long days) {
        if (days < 0) {
            long late = Math.abs(days);
            return "vencido h\u00e1 " + late + (late == 1 ? " dia" : " dias");
        }
        if (days == 0) return "vence hoje";
        return "vence em " + days + (days == 1 ? " dia" : " dias");
    }

    private static boolean alreadyNotifiedToday(Context context, List<ExpiryRow> rows) {
        return noticeKey(rows).equals(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LAST_NOTICE, ""));
    }

    private static void rememberNotifiedToday(Context context, List<ExpiryRow> rows) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LAST_NOTICE, noticeKey(rows)).apply();
    }

    private static String noticeKey(List<ExpiryRow> rows) {
        StringBuilder key = new StringBuilder(dayKey(System.currentTimeMillis()));
        for (ExpiryRow row : rows) key.append('|').append(row.id).append(':').append(row.days);
        return key.toString();
    }

    private static String dayKey(long when) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(when);
        return String.format(Locale.ROOT, "%04d%02d%02d", date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
    }

    private static long nextCheckAt() {
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, 9);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_MONTH, 1);
        return next.getTimeInMillis();
    }

    private static long startOfDay(long when) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(when);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTimeInMillis();
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, StockExpiryReceiver.class);
        intent.setAction(ACTION_CHECK);
        return PendingIntent.getBroadcast(context, REQUEST_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static class ExpiryRow {
        final String id;
        final String name;
        final long days;

        ExpiryRow(String id, String name, long days) {
            this.id = id;
            this.name = name;
            this.days = days;
        }
    }
}