package com.example.tracknote;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tracknote.Dao.NotesDao;
import com.example.tracknote.Entity.Notes;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class AddNote extends BottomSheetDialogFragment {
    EditText title, desc;
    Button save;
    ImageButton bold, italic, underline, back;
    int noteId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_note, container, false);

        title = view.findViewById(R.id.etTitle);
        desc = view.findViewById(R.id.etContent);

        back = view.findViewById(R.id.back);
        bold = view.findViewById(R.id.btnBold);
        italic = view.findViewById(R.id.btnItalic);
        underline = view.findViewById(R.id.btnUnderline);
        save = view.findViewById(R.id.btnSave);

        // If editing existing note
        if (getArguments() != null) {
            noteId = getArguments().getInt("noteId", -1);
            if (noteId != -1) {
                loadNoteForEditing(noteId);
            }
        }
        back.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Home.class);
            startActivity(intent);
            requireActivity().finish();

        });
        setupFormatting();
        save.setOnClickListener(v -> saveNotes());
        return view;
    }

    private void loadNoteForEditing(int id) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getINSTANCE(requireContext());
            NotesDao dao = db.notesDao();
            Notes note = dao.getNotesById(id);

            if (note != null) {
                requireActivity().runOnUiThread(() -> {
                    title.setText(note.title);
                    desc.setText(Html.fromHtml(note.descNote, Html.FROM_HTML_MODE_LEGACY));
                });
            }
        }).start();
    }

    private void saveNotes() {
        String Title = title.getText().toString().trim();
        String Desc = Html.toHtml(desc.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL).trim();

        if (Title.isEmpty() || Desc.isEmpty()) {
            Toast.makeText(requireContext(), "Fill both the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getINSTANCE(requireContext());
            NotesDao dao = db.notesDao();
            SessionManager session = new SessionManager(requireContext());
            long result = -1;

            if (noteId != -1) {
                // Update existing note
                Notes existingNote = dao.getNotesById(noteId);
                if (existingNote != null) {
                    existingNote.title = Title;
                    existingNote.descNote = Desc;
                    result = dao.updateNote(existingNote);
                }
            } else {
                // Insert new note
                Notes newNote = new Notes();
                newNote.title = Title;
                newNote.descNote = Desc;
                newNote.createdAt = String.valueOf(System.currentTimeMillis());
                newNote.User_local_id = session.getUserId();
                result = dao.insertNotes(newNote);
            }

            long finalResult = result;
            requireActivity().runOnUiThread(() -> {
                if (finalResult > 0) {
                    Toast.makeText(requireContext(), noteId != -1 ? "Note updated!" : "Note saved!", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof Home) {
                        ((Home) getActivity()).reloadNotes();
                    }
                    dismiss();

                } else {
                    Toast.makeText(requireContext(), "Failed to save/update note.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void setupFormatting() {
        bold.setOnClickListener(v -> toggleBold(desc));
        italic.setOnClickListener(v -> toggleItalic(desc));
        underline.setOnClickListener(v -> toggleUnderline(desc));
    }
    private void toggleBold(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Spannable str = editText.getText();

        StyleSpan[] spans = str.getSpans(start, end, StyleSpan.class);
        boolean exists = false;

        for (StyleSpan span : spans) {
            if (span.getStyle() == Typeface.BOLD) {
                str.removeSpan(span);
                exists = true;
            }
        }

        if (!exists) {
            str.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    private void toggleItalic(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Spannable str = editText.getText();

        StyleSpan[] spans = str.getSpans(start, end, StyleSpan.class);
        boolean exists = false;

        for (StyleSpan span : spans) {
            if (span.getStyle() == Typeface.ITALIC) {
                str.removeSpan(span);
                exists = true;
            }
        }

        if (!exists) {
            str.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    private void toggleUnderline(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Spannable str = editText.getText();

        UnderlineSpan[] spans = str.getSpans(start, end, UnderlineSpan.class);
        boolean exists = false;

        for (UnderlineSpan span : spans) {
            str.removeSpan(span);
            exists = true;
        }

        if (!exists) {
            str.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

}

