package com.example.tracknote;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateManager.checkForUpdate(this);

        SessionManager session = new SessionManager(this);

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, Home.class));
        } else {
            startActivity(new Intent(this, Login.class));
        }
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();
        UpdateManager.checkForUpdate(this);
    }

}
