package com.wayamba.sportms.dto;


public class SimpleMessageResponse {
    private String message;
    private String userId;

    public SimpleMessageResponse() {
    }

    public SimpleMessageResponse(String message) {
        this.message = message;
    }

    public SimpleMessageResponse(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

