package com.example.tracknote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;


import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateManager {

    private static final String UPDATE_URL =
            "https://raw.githubusercontent.com/angel-0210/TrackNote/master/update.json";
    public static void checkForUpdate(Activity activity) {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                JSONObject obj = new JSONObject(json.toString());
                int latestVersion = obj.getInt("versionCode");
                String apkUrl = obj.getString("apkUrl");

                if (latestVersion > BuildConfig.VERSION_CODE) {
                    activity.runOnUiThread(() ->
                            showUpdateDialog(activity, apkUrl));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void showUpdateDialog(Activity activity, String apkUrl) {

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return; //  DO NOT show dialog
        }

        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;

            new AlertDialog.Builder(activity)
                    .setTitle("Update Available")
                    .setMessage("A new version is available. Update now?")
                    .setCancelable(false)
                    .setPositiveButton("Update", (d, w) -> {
                        downloadAndInstall(activity, apkUrl);
                    })
                    .setNegativeButton("Later", null)
                    .show();
        });
    }


    private static void downloadAndInstall(Activity activity, String apkUrl) {
        new Thread(() -> {
            try {
                File apk = new File(
                        activity.getExternalFilesDir(null),
                        "tracknote_update.apk");

                HttpURLConnection conn =
                        (HttpURLConnection) new URL(apkUrl).openConnection();
                conn.connect();

                InputStream in = conn.getInputStream();
                FileOutputStream out = new FileOutputStream(apk);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                out.close();
                in.close();

                Uri uri = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".provider",
                        apk);

                // Android 8+ permission check
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!activity.getPackageManager().canRequestPackageInstalls()) {
                        Intent intent = new Intent(
                                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivity(intent);
                        return;
                    }
                }

                // Launch installer
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
