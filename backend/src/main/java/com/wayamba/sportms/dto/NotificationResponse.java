package com.wayamba.sportms.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String message;
    private boolean read;
    private String createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, String message, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt != null ? createdAt.toString() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

