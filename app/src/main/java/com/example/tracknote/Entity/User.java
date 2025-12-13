package com.example.tracknote.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int local_User_id;

    public String name;
    public String email;
    public String password;

    public String createdAt;

}
