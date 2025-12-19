package com.example.tracknote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.tracknote.Entity.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionManager {

    private static final String PREF_NAME = "note_app_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "local_user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_JOINED_DATE = "joined_date";
    private static final String KEY_FIREBASE_UID = "firebase_uid";
    private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        // Automatically reset old dummy user data on a new install
        if (sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)) {
            clearSession();
            editor.putBoolean(KEY_IS_FIRST_LAUNCH, false);
            editor.apply();
        }
    }

    // Create session at login/registration
    public void createSession(int userId, String username, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);

        if (!sharedPreferences.contains(KEY_JOINED_DATE)) {
            String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(new Date());
            editor.putString(KEY_JOINED_DATE, currentDate);
        }

        editor.apply();
    }

    // Save/update local user ID
    public void saveUserLocalId(User user) {
        editor.putInt(KEY_USER_ID, user.getLocal_User_id());
        editor.apply();
    }

    // Firebase UID handling
    public void saveFirebaseUid(String firebaseUid) {
        editor.putString(KEY_FIREBASE_UID, firebaseUid);
        editor.apply();
    }

    public String getFirebaseUid() {
        return sharedPreferences.getString(KEY_FIREBASE_UID, null);
    }

    public boolean hasFirebaseUid() {
        return getFirebaseUid() != null && !getFirebaseUid().isEmpty();
    }

    public void clearFirebaseUid() {
        editor.remove(KEY_FIREBASE_UID);
        editor.apply();
    }

    // Username update
    public void saveUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    // Redirect if session invalid
    public int getUserIdOrRedirect(Context context) {
        int id = getUserId();
        if (id == -1) {
            Intent intent = new Intent(context, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
        return id;
    }

    // Session getters
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getJoinedDate() {
        return sharedPreferences.getString(KEY_JOINED_DATE, "Unknown");
    }

    // Safe logout
    public void logout(Context context) {
        editor.clear();
        editor.apply();

        Intent intent = new Intent(context, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
