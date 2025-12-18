package com.example.tracknote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;

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

public class Register extends AppCompatActivity {

    private EditText name, email, password;
    private Button sign;
    private TextView loglink;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        sign = findViewById(R.id.sign_up_button);
        loglink = findViewById(R.id.login_link);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loglink.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });

        sign.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String Name = name.getText().toString().trim();
        String Mail = email.getText().toString().trim();
        String Pass = password.getText().toString().trim();

        if (Name.isEmpty() || Mail.isEmpty() || Pass.isEmpty()) {
            toast("All fields are required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(Mail).matches()) {
            email.setError("Invalid email");
            return;
        }

        if (Pass.length() < 6) {
            password.setError("Minimum 6 characters");
            return;
        }

        // STEP 1: Firebase Auth
        auth.createUserWithEmailAndPassword(Mail, Pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        toast("Firebase error: user null");
                        return;
                    }

                    String firebaseUid = firebaseUser.getUid();
                    saveUserLocallyAndCloud(firebaseUid, Name, Mail, Pass);

                })
                .addOnFailureListener(e -> toast("Firebase failed: " + e.getMessage()));
    }

    private void saveUserLocallyAndCloud(String firebaseUid, String name, String email, String plainPass) {

        // 1️⃣ Save to Room in background
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserDao userDao = db.userDao();

            if (userDao.getUserByEmail(email) != null) {
                runOnUiThread(() -> toast("Email already registered"));
                return;
            }

            String hashedPassword = BCrypt.hashpw(plainPass, BCrypt.gensalt());

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(hashedPassword);
            user.setFirebaseUid(firebaseUid);
            user.setLastModified(System.currentTimeMillis());

            long localId = userDao.insert(user);

            // 2️⃣ NOW switch to MAIN thread for Firestore
            runOnUiThread(() -> saveUserToFirestore(firebaseUid, name, email, localId));

        }).start();
    }
    private void saveUserToFirestore(String firebaseUid, String name, String email, long localId) {

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("createdAt", System.currentTimeMillis());

        firestore.collection("users")
                .document(firebaseUid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    SessionManager session = new SessionManager(this);
                    session.createSession((int) localId, name, email);
                    session.saveFirebaseUid(firebaseUid);

                    toast("Registration successful");
                    startActivity(new Intent(Register.this, Login.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        toast("Failed to save in Firestore: " + e.getMessage())
                );
    }



    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
