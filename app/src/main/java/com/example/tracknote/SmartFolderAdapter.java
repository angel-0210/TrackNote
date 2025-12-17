package com.example.tracknote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracknote.R;

import java.util.List;

public class SmartFolderAdapter extends RecyclerView.Adapter<SmartFolderAdapter.ViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(SmartFolderType type);
    }

    public enum SmartFolderType {
        ALL,
        PINNED,
        RECENT,
        TODAY
    }

    private final List<SmartFolderType> folders;
    private final OnFolderClickListener listener;

    public SmartFolderAdapter(List<SmartFolderType> folders,
                              OnFolderClickListener listener) {
        this.folders = folders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_smart_folder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmartFolderType type = folders.get(position);

        holder.title.setText(getTitle(type));

        holder.itemView.setOnClickListener(v ->
                listener.onFolderClick(type)
        );
    }

    private String getTitle(SmartFolderType type) {
        switch (type) {
            case PINNED:
                return " Pinned";
            case RECENT:
                return " Recent";
            case TODAY:
                return " Today";
            default:
                return " All";
        }
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvFolder);
        }
    }
}
