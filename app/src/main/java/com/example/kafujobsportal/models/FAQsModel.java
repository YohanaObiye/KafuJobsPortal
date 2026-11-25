package com.example.kafujobsportal.models;

public class FAQsModel {
    private String id;
    private String question;
    private String answer;
    private long createdAt;

    // Empty constructor for Firebase
    public FAQsModel() {
    }

    public FAQsModel(String id, String question, String answer, long createdAt) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
