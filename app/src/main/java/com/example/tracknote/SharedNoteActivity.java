package com.example.tracknote;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SharedNoteActivity extends AppCompatActivity {

    private TextView tvTitle, tvDesc, tvCategory;
    private View root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_note);

        //  Bind views
        tvTitle = findViewById(R.id.tvTitle);
        tvDesc = findViewById(R.id.tvContent);
        tvCategory = findViewById(R.id.tvCategory);
        root = findViewById(R.id.root);

        ImageButton btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> finish());

        // 🔐 User must be logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to view shared note", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Handle deep link: tracknote://share/{shareId}
        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, "Invalid share link", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String shareId = getIntent().getData().getLastPathSegment();

        if (shareId == null || shareId.isEmpty()) {
            Toast.makeText(this, "Invalid share link", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadSharedNote(shareId);

    }

    private void loadSharedNote(String shareId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1️. Fetch share metadata
        db.collection("shared_notes")
                .document(shareId)
                .get()
                .addOnSuccessListener(shareDoc -> {

                    if (!shareDoc.exists()) {
                        Toast.makeText(this, "Shared note not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String ownerUid = shareDoc.getString("ownerUid");
                    String noteId = shareDoc.getString("noteId");

                    // 2️⃣ Fetch actual note
                    db.collection("users")
                            .document(ownerUid)
                            .collection("notes")
                            .document(noteId)
                            .get()
                            .addOnSuccessListener(noteDoc -> {

                                if (!noteDoc.exists()) {
                                    Toast.makeText(this, "Note no longer exists", Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                }

                                // 🔹 Populate UI (READ-ONLY)
                                tvTitle.setText(noteDoc.getString("title"));
                                tvDesc.setText(
                                        Html.fromHtml(
                                                noteDoc.getString("desc"),
                                                Html.FROM_HTML_MODE_LEGACY
                                        )
                                );

                                tvCategory.setText(noteDoc.getString("category"));

                                Long color = noteDoc.getLong("color");
                                if (color != null) {
                                    root.setBackgroundColor(color.intValue());
                                } else {
                                    root.setBackgroundColor(Color.WHITE);
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Invalid or expired share link", Toast.LENGTH_SHORT).show()
                );
    }
}
