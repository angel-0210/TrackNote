package com.example.tracknote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_INSTALLED_VERSION = "installed_version";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for update dialog
        UpdateManager.checkForUpdate(this);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersion = prefs.getInt(KEY_INSTALLED_VERSION, -1);
        int currentVersion = BuildConfig.VERSION_CODE;

        Log.d("LauncherActivity", "Saved version: " + savedVersion);
        Log.d("LauncherActivity", "Current version: " + currentVersion);

        boolean isNewInstallOrUpdate = savedVersion < currentVersion;

        if (isNewInstallOrUpdate) {
            // First launch after install or update → show Register
            startActivity(new Intent(this, Register.class));

            // Save current version
            prefs.edit().putInt(KEY_INSTALLED_VERSION, currentVersion).apply();
        } else {
            // Not first launch → check session
            SessionManager session = new SessionManager(this);

            if (session.isLoggedIn()) {
                startActivity(new Intent(this, Home.class));
            } else {
                startActivity(new Intent(this, Login.class));
            }
        }

        finish();
    }
}
