package com.example.tracknote.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tracknote.Entity.Notes;
import com.example.tracknote.Entity.User;

import java.util.List;

@Dao
public interface NotesDao {
    @Insert
    long insertNotes(Notes notes);
    @Update
    int updateNote(Notes notes);


    @Delete
    void deleteNote(Notes notes);

    @Query("SELECT * FROM notes WHERE local_note_id = :id LIMIT 1")
    Notes getNotesById(int id);
    @Query("SELECT * FROM notes WHERE User_local_id = :userId")
    List<Notes> getNotesForUser(int userId);

    @Query("SELECT * FROM notes WHERE User_local_id = :userId AND (LOWER(title) LIKE LOWER('%' || :query || '%') OR LOWER(descNote) LIKE LOWER('%' || :query || '%'))")
    List<Notes> searchNotes(String query, int userId);


}
