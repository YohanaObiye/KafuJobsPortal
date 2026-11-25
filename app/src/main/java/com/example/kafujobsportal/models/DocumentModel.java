package com.example.kafujobsportal.models;

public class DocumentModel {
    private final String name;
    private final String url;

    public DocumentModel(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
