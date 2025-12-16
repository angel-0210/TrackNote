package com.example.tracknote.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int local_User_id;
    private String firebaseUid; // Firebase Auth UID
    public String name;
    public String email;
    public String password;

    public String createdAt;
    private long lastModified; // timestamp for sync
    // Empty constructor
    public User() {}

    // Full constructor
    public User(String firebaseUid, String name, String email, String password, long lastModified) {
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.lastModified = lastModified;
    }

    // Getters and Setters
    public int getLocal_User_id() { return local_User_id; }
    public void setLocal_User_id(int local_User_id) { this.local_User_id = local_User_id; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
}
