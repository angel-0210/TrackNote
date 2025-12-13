package com.example.tracknote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Account extends BaseActivity {

    private ImageButton backButton;
    private ImageView profileImg;
    private TextView tvUser, mail, activeSinceText;
    private Button logoutButton;

    private SessionManager sessionManager;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ICON = "user_icon_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        backButton = findViewById(R.id.back);
        profileImg = findViewById(R.id.profileImg);
        tvUser = findViewById(R.id.tvUser);
        mail = findViewById(R.id.mail);
        activeSinceText = findViewById(R.id.activeSinceText);
        logoutButton = findViewById(R.id.logoutButton);

        sessionManager = new SessionManager(this);

        // Set user data from
        tvUser.setText(sessionManager.getUsername());
        mail.setText(sessionManager.getEmail());

        String joinedDate = sessionManager.getJoinedDate();
        String relativeTime = getTimeSinceJoined(joinedDate);
        activeSinceText.setText("Active Since : " + relativeTime);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        // ActivityResultLauncher for image picking
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        // 3. Update: Passed Uri object to the corrected save method
                        saveImageUri(imageUri);
                        loadImage(); // Refresh with new image
                    }
                }
        );

        // Load saved image
        loadImage();


        backButton.setOnClickListener(v -> {
            startActivity(new Intent(Account.this, Home.class));
            finish();
        });

        // **<<--- CRITICAL FIX 1: Add read permission flag --->>**
        profileImg.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // CRITICAL LINE: Grants temporary read permission to the URI.
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });
        // **<<--- END CRITICAL FIX 1 --->>**

        logoutButton.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(Account.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // **<<--- CRITICAL FIX 2: Implement persistent URI access --->>**
    private void saveImageUri(Uri uri) {
        // Updated method signature from String to Uri
        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {
            // This grants persistent read access, allowing Glide to load the image later.
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        // Save the URI as a string
        prefs.edit().putString(KEY_USER_ICON, uri.toString()).apply();
    }
    // **<<--- END CRITICAL FIX 2 --->>**


    private void loadImage() {
        String uri = prefs.getString(KEY_USER_ICON, null);
        if (uri != null) {
            Glide.with(this)
                    .load(Uri.parse(uri))
                    .placeholder(R.drawable.profile1) // while loading
                    .error(R.drawable.profile1) // if failed
                    .circleCrop() // round profile picture
                    .into(profileImg);
        } else {
            profileImg.setImageResource(R.drawable.profile1);
        }
    }
    private String getTimeSinceJoined(String joinedDateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date joinedDate = sdf.parse(joinedDateString);
            Date currentDate = new Date();

            long diffInMillis = currentDate.getTime() - joinedDate.getTime();
            long days = diffInMillis / (1000 * 60 * 60 * 24);

            if (days < 30) {
                return days + " days ago";
            } else if (days < 365) {
                return (days / 30) + " months ago";
            } else {
                return (days / 365) + " years ago";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}