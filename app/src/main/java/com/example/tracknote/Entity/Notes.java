package com.example.tracknote.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(
        tableName = "notes",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "local_User_id",
                childColumns = "User_local_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("User_local_id")}
)
public class Notes {

        @PrimaryKey(autoGenerate = true)
        private int local_note_id;

        private Integer User_local_id; // FK

        @NonNull
        @ColumnInfo(defaultValue = "")
        private String cloudNoteId; // must be initialized

        @NonNull
        private String title;

        private String descNote;
        private long createdAt;
        private String category;
        private String tags;
        private int color;

        @ColumnInfo(defaultValue = "0")
        private int isPinned;

        private long lastModified;

        @ColumnInfo(defaultValue = "0")
        private int isDeleted;

        public Notes() {
                // Ensure cloudNoteId is never null
                this.cloudNoteId = UUID.randomUUID().toString();
                this.isDeleted = 0;
        }

        // Getters and setters
        public int getLocal_note_id() { return local_note_id; }
        public void setLocal_note_id(int local_note_id) { this.local_note_id = local_note_id; }

        public Integer getUser_local_id() { return User_local_id; }
        public void setUser_local_id(Integer user_local_id) { User_local_id = user_local_id; }


        @NonNull
        public String getCloudNoteId() { return cloudNoteId; }
        public void setCloudNoteId(@NonNull String cloudNoteId) { this.cloudNoteId = cloudNoteId; }

        public int getIsDeleted() { return isDeleted; }
        public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }

        @NonNull
        public String getTitle() { return title; }
        public void setTitle(@NonNull String title) { this.title = title; }

        public String getDescNote() { return descNote; }
        public void setDescNote(String descNote) { this.descNote = descNote; }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public int getColor() { return color; }
        public void setColor(int color) { this.color = color; }

        public int getIsPinned() { return isPinned; }
        public void setIsPinned(int isPinned) { this.isPinned = isPinned; }

        public long getLastModified() { return lastModified; }
        public void setLastModified(long lastModified) { this.lastModified = lastModified; }
}
