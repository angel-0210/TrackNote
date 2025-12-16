package com.example.tracknote.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tracknote.Entity.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Update
    void updateUser(User user);


    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM User WHERE firebaseUid = :firebaseUid LIMIT 1")
    User getUserByFirebaseUid(String firebaseUid);
    @Query("SELECT * FROM User WHERE local_user_id = :id LIMIT 1")
    User getUserById(int id);

    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT COUNT(*) FROM user WHERE local_User_id = :id")
    int userExists(int id);


}
