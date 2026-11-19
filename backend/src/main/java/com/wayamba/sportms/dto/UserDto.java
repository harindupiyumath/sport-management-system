package com.wayamba.sportms.dto;

public class UserDto {
    private String userId;
    private String role;
    private String faculty;

    public UserDto() {
    }

    public UserDto(String userId, String role, String faculty) {
        this.userId = userId;
        this.role = role;
        this.faculty = faculty;
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
}

