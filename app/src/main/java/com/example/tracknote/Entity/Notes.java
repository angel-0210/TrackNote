package com.example.tracknote.Entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes",foreignKeys =
        @ForeignKey(
                entity = User.class,
                parentColumns ="local_User_id",
                childColumns = "User_local_id",
                onDelete =ForeignKey.CASCADE
        ),
        indices ={@Index("User_local_id")}

)
public class Notes {
        @PrimaryKey(autoGenerate = true)
        public int local_note_id;
        public int User_local_id; //FK

        public String title;
        public String descNote;
        public String createdAt;


}
