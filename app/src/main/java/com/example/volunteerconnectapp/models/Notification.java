package com.example.volunteerconnectapp.models;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private boolean isRead;
    private String createdAt;

    public Notification(int id, int userId, String message, boolean isRead, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }


    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }


    public void setRead(boolean read) { isRead = read; }
}