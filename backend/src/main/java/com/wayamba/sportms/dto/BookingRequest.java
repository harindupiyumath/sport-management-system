package com.wayamba.sportms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotNull(message = "Sport ID is required")
    private Long sportId;

    @NotBlank(message = "Date is required")
    private String date; // YYYY-MM-DD

    @NotBlank(message = "Start time is required")
    private String start; // HH:mm

    @NotBlank(message = "End time is required")
    private String end; // HH:mm

    public BookingRequest() {
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
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
}

