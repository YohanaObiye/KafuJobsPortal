package com.example.kafujobsportal.models;

public class UploadModel {
    public String applicationId, docId, url;
    public long createdAt;

    public UploadModel() {
    }

    public UploadModel(String applicationId, String docId, String url, long createdAt) {
        this.applicationId = applicationId;
        this.docId = docId;
        this.url = url;
        this.createdAt = createdAt;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getDocId() {
        return docId;
    }

    public String getUrl() {
        return url;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
