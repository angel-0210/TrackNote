package com.example.tracknote;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracknote.Entity.Notes;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    private List<Notes> notesList;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Notes note);
    }

    public NotesAdapter(List<Notes> notesList, OnNoteClickListener listener) {
        this.notesList = notesList;
        this.listener = listener;
    }

    public void setNotesList(List<Notes> list){
        this.notesList=list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public NotesAdapter.NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item,parent,false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.NotesViewHolder holder, int position) {
        Notes note=notesList.get(position);
        holder.title.setText(note.title);
        holder.desc.setText(android.text.Html.fromHtml(note.descNote, android.text.Html.FROM_HTML_MODE_LEGACY));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesList != null ? notesList.size() : 0;
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        TextView title,desc;
        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.tvTitle);
            desc=itemView.findViewById(R.id.tvContent);
        }
    }
    public Notes getNoteAt(int position) {
        return notesList.get(position);
    }
    public void setNotes(List<Notes> notelist){
        this.notesList=notelist;
    }
}
