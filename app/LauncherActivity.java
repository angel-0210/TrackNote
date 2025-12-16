package com.example.tracknote;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, Home.class));
        } else {
            startActivity(new Intent(this, Login.class));
        }
        finish();
    }
}

