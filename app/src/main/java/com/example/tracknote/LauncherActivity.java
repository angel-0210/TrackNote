package com.example.tracknote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for updates
        UpdateManager.checkForUpdate(this);

        // Check if first launch
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            // First install → open Register
            startActivity(new Intent(this, Register.class));

            // Mark first launch as done
            prefs.edit().putBoolean("isFirstLaunch", false).apply();
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
