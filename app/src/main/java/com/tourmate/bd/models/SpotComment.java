package com.tourmate.bd.models;

import java.io.Serializable;

/**
 * Model representing a user comment or developer feedback for a tourist spot in Firebase Realtime Database.
 * Stored under /spot_comments/{spot_id}/{comment_id}.
 */
public class SpotComment implements Serializable {

    private String commentId;
    private String spotId;
    private String userUid;
    private String userName;
    private String userEmail;
    private String commentText;
    private long timestamp;

    // Required empty constructor for Firebase Realtime Database deserialization
    public SpotComment() {
    }

    public SpotComment(String commentId, String spotId, String userUid, String userName, String userEmail, String commentText, long timestamp) {
        this.commentId = commentId;
        this.spotId = spotId;
        this.userUid = userUid;
        this.userName = userName;
        this.userEmail = userEmail;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
