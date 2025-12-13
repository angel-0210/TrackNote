package com.example.tracknote;

import android.content.Intent;
import android.os.Bundle;

import com.example.tracknote.Dao.UserDao;
import com.example.tracknote.Entity.User;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Patterns;

import org.mindrot.jbcrypt.BCrypt;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Register extends AppCompatActivity {
    private EditText name, email, password;
    private Button sign;
    private TextView loglink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        sign = findViewById(R.id.sign_up_button);

        loglink = findViewById(R.id.login_link);
        loglink.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
        sign.setOnClickListener(v -> registerUser());

    }

    private void registerUser() {
        String Name = name.getText().toString().trim();
        String Mail = email.getText().toString().trim();
        String Pass = password.getText().toString().trim();
        // Hash the password
        String hashedPassword = BCrypt.hashpw(Pass, BCrypt.gensalt(12));
        // Empty fields check
        if (Name.isEmpty() || Mail.isEmpty() || Pass.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email validation
        if (!Patterns.EMAIL_ADDRESS.matcher(Mail).matches()) {
            email.setError("Please enter a valid email");
            return;
        } else {
            email.setError(null); // Clear error
        }

        // Password validation
        if (Pass.length() < 6) {
            password.setError("Password must be at least 6 characters long");
            return;
        } else {
            password.setError(null); // Clear error
        }

            new Thread(() -> {
                AppDatabase db = AppDatabase.getINSTANCE(getApplicationContext());
                UserDao userDao = db.userDao();

                // Check if email already exists
                User existingUser = userDao.getUserByEmail(Mail);
                if (existingUser != null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // Create and insert new user
                User newUser = new User();
                newUser.name = Name;
                newUser.email = Mail;
                newUser.password = hashedPassword;
                newUser.createdAt = String.valueOf(System.currentTimeMillis());

                userDao.insertUser(newUser);

                // Now retrieve the inserted user to get auto-generated ID
                User insertedUser = userDao.getUserByEmail(Mail);

                if (insertedUser == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // Create session using correct field: local_User_id
                SessionManager session = new SessionManager(getApplicationContext());
                session.createSession(insertedUser.local_User_id, insertedUser.name, insertedUser.email);
                session.saveUserLocalId(insertedUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Register.this, Home.class);
                    startActivity(intent);
                    finish();
                });
            }).start();
        }


    }

