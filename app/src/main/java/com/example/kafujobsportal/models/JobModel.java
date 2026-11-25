package com.example.kafujobsportal.models;

public class JobModel {
    private String id;
    private String title;
    private String description;
    private String status;
    private String dueDate;
    private long createdAt;
    private String supportingDocumentUrl;

    public JobModel() {
    }

    public JobModel(String id, String title, String description, String status, String dueDate, long createdAt, String supportingDocumentUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.supportingDocumentUrl = supportingDocumentUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getSupportingDocumentUrl() {
        return supportingDocumentUrl;
    }
}