package com.example.tracknote;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.tracknote.Dao.UserDao;
import com.example.tracknote.Entity.User;
import com.example.tracknote.AppDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {

    private final FirebaseAuth auth;
    private final UserDao userDao;
    private final Context context;

    public AuthManager(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.userDao = AppDatabase.getInstance(context).userDao();
    }

    public void signInAnonymously(OnAuthCompleteListener listener) {
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    // Check if user exists in local DB
                    User existingUser = userDao.getUserByFirebaseUid(uid);
                    if (existingUser == null) {
                        // Insert new user locally
                        User newUser = new User();
                        newUser.setFirebaseUid(uid);
                        newUser.setName("Anonymous");
                        newUser.setEmail("");
                        newUser.setLastModified(System.currentTimeMillis());
                        new Thread(() -> userDao.insert(newUser)).start();
                    }
                    listener.onSuccess(uid);
                }
            } else {
                listener.onFailure(task.getException());
            }
        });
    }

    public interface OnAuthCompleteListener {
        void onSuccess(String firebaseUid);
        void onFailure(Exception e);
    }
}
