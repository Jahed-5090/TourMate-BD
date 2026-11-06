package com.tourmate.bd.models;

import java.io.Serializable;

/**
 * Model representing a registered or authenticated user in Firebase Realtime Database.
 * Synced under /users/{uid} in the Firebase Cloud Server.
 */
public class UserProfile implements Serializable {

    private String uid;
    private String name;
    private String email;
    private String photoUrl;
    private String authProvider;
    private long createdAt;
    private long lastLoginAt;

    // Default no-argument constructor required for Firebase Realtime Database deserialization
    public UserProfile() {
    }

    public UserProfile(String uid, String name, String email, String photoUrl, String authProvider, long createdAt, long lastLoginAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.authProvider = authProvider;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}

