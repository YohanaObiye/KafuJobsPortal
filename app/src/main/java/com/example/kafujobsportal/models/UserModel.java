package com.example.kafujobsportal.models;

public class UserModel {
    private String id;
    private String username;
    private String email;
    private String role;
    private long createdAt;
    private String phone;
    private String profile_image;

    public UserModel() {
    }

    public UserModel(String id, String username, String email, String role, long createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileImage() {
        return phone;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
