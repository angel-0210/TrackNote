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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    EditText mail, pass;
    Button log;
    TextView signUp;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mail = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        log = findViewById(R.id.login_button);
        signUp = findViewById(R.id.sign_up_link);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        SessionManager session = new SessionManager(Login.this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(Login.this, Home.class));
            finish();
        }

        log.setOnClickListener(v -> login());
        signUp.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Register.class));
            finish();
        });
    }

    private void login() {
        String email = mail.getText().toString().trim();
        String passwordPlain = pass.getText().toString().trim();

        if (email.isEmpty() || passwordPlain.isEmpty()) {
            Toast.makeText(this, "Please fill both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserDao userDao = db.userDao();

            // Offline login using Room
            User loggedUser = userDao.findByEmail(email);
            boolean isValid = loggedUser != null && BCrypt.checkpw(passwordPlain, loggedUser.getPassword());

            runOnUiThread(() -> {
                if (isValid) {

                    // Save session locally
                    SessionManager session = new SessionManager(Login.this);
                    session.createSession(loggedUser.getLocal_User_id(), loggedUser.getName(), loggedUser.getEmail());
                    session.saveUserLocalId(loggedUser);

                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    // Firebase Auth login for cloud sync
                    firebaseLoginSync(loggedUser, passwordPlain);

                } else {
                    Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void firebaseLoginSync(User user, String plainPassword) {

        auth.signInWithEmailAndPassword(user.getEmail(), plainPassword)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) return;

                    String firebaseUid = firebaseUser.getUid();
                    user.setFirebaseUid(firebaseUid);

                    // ✅ SAVE Firebase UID IN SESSION (MANDATORY)
                    SessionManager session = new SessionManager(Login.this);
                    session.saveFirebaseUid(firebaseUid);

                    // ✅ SAFE Firestore user sync
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", user.getName());
                    data.put("email", user.getEmail());
                    data.put("lastModified", System.currentTimeMillis());

                    firestore.collection("users")
                            .document(firebaseUid)
                            .set(data);

                    // ✅ ONE-TIME pull (no listener here)
                    SyncManager syncManager =
                            new SyncManager(getApplicationContext(), firebaseUid);
                    syncManager.pullNotesFromCloud();

                    // Move to Home
                    startActivity(new Intent(Login.this, Home.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cloud sync failed", Toast.LENGTH_SHORT).show()
                );
    }
}
