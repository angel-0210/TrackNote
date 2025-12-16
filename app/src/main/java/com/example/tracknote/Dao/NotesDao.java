package com.example.tracknote.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tracknote.Entity.Notes;

import java.util.List;

@Dao
public interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notes note);

    @Update
    int updateNote(Notes note);

    @Query("DELETE FROM notes WHERE cloudNoteId = :cloudId")
    void deleteByCloudId(String cloudId);
    @Query("UPDATE notes SET isDeleted = 1 WHERE local_note_id = :noteId")
    void softDelete(int noteId);


    @Query("SELECT * FROM notes WHERE User_local_id = :userId AND isDeleted = 0 ORDER BY isPinned DESC, createdAt DESC")
    LiveData<List<Notes>> getNotesForUser(int userId);

    @Query("SELECT * FROM notes WHERE User_local_id = :userId AND isDeleted = 0 AND category = :cat ORDER BY createdAt DESC")
    LiveData<List<Notes>> getNotesByCategory(int userId, String cat);

    @Query("SELECT DISTINCT category FROM notes WHERE User_local_id = :userId AND category IS NOT NULL AND category != ''")
    LiveData<List<String>> getUserCategories(int userId);


    @Query("SELECT * FROM notes WHERE local_note_id = :noteId LIMIT 1")
    Notes getNoteById(int noteId);

    @Query("SELECT * FROM notes WHERE cloudNoteId = :cloudId LIMIT 1")
    Notes getByCloudId(String cloudId);
}
