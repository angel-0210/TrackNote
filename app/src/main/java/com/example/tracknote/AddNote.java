package com.example.tracknote;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.example.tracknote.Dao.NotesDao;
import com.example.tracknote.Entity.Notes;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddNote extends BottomSheetDialogFragment {

    private EditText title, desc;
    private AutoCompleteTextView category;
    private View root;
    private  ImageButton pin;
    private ImageButton btnBold, btnItalic, btnUnderline;
    private int noteId = -1;
    private int color = Color.WHITE;
    private int isPinned = 0;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.add_note, container, false);

        title = root.findViewById(R.id.etTitle);
        desc = root.findViewById(R.id.etContent);
        category = root.findViewById(R.id.etCategory);

        pin = root.findViewById(R.id.btnPin);
        ImageButton back = root.findViewById(R.id.back);
        ImageButton colorBtn = root.findViewById(R.id.btnColor);
        MaterialButton save = root.findViewById(R.id.btnSave);

        //  ADDED: init formatting buttons
        btnBold = root.findViewById(R.id.btnBold);
        btnItalic = root.findViewById(R.id.btnItalic);
        btnUnderline = root.findViewById(R.id.btnUnderline);

        SessionManager session = new SessionManager(requireContext());
        NotesDao dao = AppDatabase.getInstance(requireContext()).notesDao();

        if (getArguments() != null) {
            noteId = getArguments().getInt("noteId", -1);
            if (noteId != -1) loadNote(dao);
        }

        pin.setOnClickListener(v -> {
            isPinned = isPinned == 0 ? 1 : 0;
            pin.setAlpha(isPinned == 1 ? 1f : 0.4f);
        });

        btnBold.setOnClickListener(v ->
                applySpan(new StyleSpan(Typeface.BOLD)));

        btnItalic.setOnClickListener(v ->
                applySpan(new StyleSpan(Typeface.ITALIC)));

        btnUnderline.setOnClickListener(v ->
                applySpan(new UnderlineSpan()));

        colorBtn.setOnClickListener(v -> showColors());
        back.setOnClickListener(v -> dismiss());
        save.setOnClickListener(v -> saveNote(session, dao));

        return root;
    }
    private void applySpan(Object span) {
        int start = desc.getSelectionStart();
        int end = desc.getSelectionEnd();

        if (start >= end) return;

        Spannable text = desc.getText();

        boolean remove = false;

        if (span instanceof StyleSpan) {
            StyleSpan styleSpan = (StyleSpan) span;
            StyleSpan[] spans = text.getSpans(start, end, StyleSpan.class);
            for (StyleSpan s : spans) {
                if (s.getStyle() == styleSpan.getStyle()) {
                    text.removeSpan(s);
                    remove = true;
                }
            }
        } else if (span instanceof UnderlineSpan) {
            UnderlineSpan[] spans = text.getSpans(start, end, UnderlineSpan.class);
            for (UnderlineSpan s : spans) {
                text.removeSpan(s);
                remove = true;
            }
        }

        if (!remove) {
            text.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void saveNote(SessionManager session, NotesDao dao) {
        new Thread(() -> {

            int userId = session.getUserId();
            String firebaseUid = session.getFirebaseUid();

            Notes note = (noteId == -1) ? new Notes() : dao.getNoteById(noteId);

            note.setTitle(title.getText().toString().trim());
            note.setDescNote(Html.toHtml(desc.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
            note.setCategory(category.getText().toString());
            note.setColor(color);
            note.setIsPinned(isPinned);
            note.setUser_local_id(userId);
            note.setLastModified(System.currentTimeMillis());

            if (note.getCreatedAt() == 0) note.setCreatedAt(System.currentTimeMillis());

            if (note.getCloudNoteId() == null || note.getCloudNoteId().isEmpty()) {
                note.setCloudNoteId(UUID.randomUUID().toString());
            }

            // --- Save locally ---
            if (noteId == -1) {
                dao.insert(note);
            } else {
                dao.updateNote(note);
            }

            // --- Push to Firestore via SyncManager ---
            SyncManager syncManager = new SyncManager(requireContext(), firebaseUid);
            syncManager.pushNoteToCloud(note);

            requireActivity().runOnUiThread(this::dismiss);

        }).start();
    }



    private void loadNote(NotesDao dao) {
        new Thread(() -> {
            Notes n = dao.getNoteById(noteId);
            requireActivity().runOnUiThread(() -> {
                title.setText(n.getTitle());
                desc.setText(Html.fromHtml(n.getDescNote()));
                category.setText(n.getCategory());
                color = n.getColor();
                isPinned = n.getIsPinned();
                root.setBackgroundColor(color);

                pin.setAlpha(isPinned == 1 ? 1f : 0.4f);
            });
        }).start();
    }

    private void showColors() {
        int[] colors = {
                Color.rgb(255, 255, 255),   // Pastel White
                Color.rgb(255, 253, 208), // Pastel Yellow
                Color.rgb(119, 221, 119),   // Pastel Green
                Color.rgb(174, 198, 207),   // Pastel Cyan/Blue
                Color.rgb(255, 179, 186)   // Pastel Pink/Magenta
        };

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        for (int c : colors) {
            View v = new View(requireContext());
            v.setBackgroundColor(c);
            v.setOnClickListener(x -> {
                color = c;
                root.setBackgroundColor(c);
            });
            layout.addView(v, new LinearLayout.LayoutParams(120,120));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose color")
                .setView(layout)
                .setPositiveButton("Done", null)
                .show();
    }
}
