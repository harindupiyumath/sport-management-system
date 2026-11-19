package com.wayamba.sportms.dto;

public class LoginResponse {
    private String token;
    private UserDto user;

    public LoginResponse() {
    }

    // True when the user must set a new password before using the app
    private boolean mustChangePassword;

    public LoginResponse(String token, UserDto user) {
        this(token, user, false);
    }

    public LoginResponse(String token, UserDto user, boolean mustChangePassword) {
        this.token = token;
        this.user = user;
        this.mustChangePassword = mustChangePassword;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}

