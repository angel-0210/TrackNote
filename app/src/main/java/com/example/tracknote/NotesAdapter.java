package com.example.tracknote;

import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracknote.Entity.Notes;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private List<Notes> notes = new ArrayList<>();
    private String searchText = "";
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Notes note);
    }

    public NotesAdapter(List<Notes> notes, OnNoteClickListener listener) {
        this.notes = notes != null ? new ArrayList<>(notes) : new ArrayList<>();
        this.listener = listener;
    }

    public void setNotesList(List<Notes> notes) {
        this.notes = notes != null ? new ArrayList<>(notes) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSearchText(String text) {
        this.searchText = text != null ? text.trim() : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notes_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Notes note = notes.get(pos);

        // Background color
        h.itemView.setBackgroundColor(
                note.getColor() != 0 ? note.getColor() : Color.WHITE
        );

        // Pin
        if (note.getIsPinned() == 1) {
            h.pin.setVisibility(View.VISIBLE);
            h.pin.setColorFilter(Color.parseColor("#FFD700"));
        } else {
            h.pin.setVisibility(View.GONE);
        }

        // Title + Content (highlight)
        h.title.setText(highlight(note.getTitle()));
        String content = Html.fromHtml(
                note.getDescNote() != null ? note.getDescNote() : ""
        ).toString();
        h.desc.setText(highlight(content));

        // Category
        h.category.setText(note.getCategory() != null ? note.getCategory() : "");

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(note);
        });
    }

    private Spannable highlight(String text) {
        if (text == null) text = "";

        SpannableString s = new SpannableString(text);
        if (searchText.isEmpty()) return s;

        String lowerText = text.toLowerCase();
        String query = searchText.toLowerCase();

        int index = lowerText.indexOf(query);
        while (index >= 0) {
            s.setSpan(
                    new BackgroundColorSpan(Color.YELLOW),
                    index,
                    index + query.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            index = lowerText.indexOf(query, index + query.length());
        }
        return s;
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
    public Notes getNoteAt(int position) {
        return notes.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, category;
        ImageView pin;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            desc = v.findViewById(R.id.tvContent);
            category = v.findViewById(R.id.tvCategory);
            pin = v.findViewById(R.id.pinIcon);
        }
    }
}