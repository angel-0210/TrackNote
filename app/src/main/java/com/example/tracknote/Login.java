package com.example.tracknote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tracknote.Dao.UserDao;
import com.example.tracknote.Entity.User;

import org.mindrot.jbcrypt.BCrypt;

public class Login extends AppCompatActivity {
    EditText mail,pass;
    Button log;
    TextView signUp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mail=findViewById(R.id.email);
        pass=findViewById(R.id.password);
        log=findViewById(R.id.login_button);
        signUp=findViewById(R.id.sign_up_link);
        SessionManager session = new SessionManager(Login.this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(Login.this, Home.class));
            finish();
        }
        log.setOnClickListener(v -> login());
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
            finish();
        });

    }
    private void login() {
        String email = mail.getText().toString().trim();
        String Pass = pass.getText().toString().trim();

        if (email.isEmpty() || Pass.isEmpty()) {
            Toast.makeText(this, "Please fill both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getINSTANCE(getApplicationContext());
            UserDao userDao = db.userDao();

            // Get user by email only
            User loggedUser = userDao.findByEmail(email);

            boolean isValid = loggedUser != null && BCrypt.checkpw(Pass, loggedUser.password);

            runOnUiThread(() -> {
                if (isValid) {
                    // Save session info
                    SessionManager session = new SessionManager(Login.this);
                    session.createSession(loggedUser.local_User_id, loggedUser.name, loggedUser.email);
                    session.saveUserLocalId(loggedUser);

                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    // Redirect to Home activity
                    Intent intent = new Intent(Login.this, Home.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

}

