package com.wayamba.sportms.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private String userId;
    private String role;
    private String faculty;
    private String createdAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(String userId, String role, String faculty, LocalDateTime createdAt) {
        this.userId = userId;
        this.role = role;
        this.faculty = faculty;
        this.createdAt = createdAt != null ? createdAt.toString() : null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

