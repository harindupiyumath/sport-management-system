package com.wayamba.sportms.dto;

public class AdminBookingResponse {
    private Long id;
    private String userId;
    private String role;
    private String sportName;
    private String date;
    private String start;
    private String end;
    private String status;

    public AdminBookingResponse() {
    }

    public AdminBookingResponse(Long id, String userId, String role, String sportName,
                                String date, String start, String end, String status) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.sportName = sportName;
        this.date = date;
        this.start = start;
        this.end = end;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

