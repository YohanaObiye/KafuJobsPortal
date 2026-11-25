package com.example.kafujobsportal.models;

public class NotificationModel {
    String id, senderId, receiverId, message;
    long timestamp;

    public NotificationModel() {
    }

    public NotificationModel(String id, String senderId, String receiverId, String message, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
