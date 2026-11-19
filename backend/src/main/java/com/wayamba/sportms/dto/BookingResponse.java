package com.wayamba.sportms.dto;


public class BookingResponse {
    private Long id;
    private Long sportId;
    private String sportName;
    private String facility;
    private String date;
    private String start;
    private String end;
    private String status;

    public BookingResponse() {
    }

    public BookingResponse(Long id, Long sportId, String sportName, String facility,
                           String date, String start, String end, String status) {
        this.id = id;
        this.sportId = sportId;
        this.sportName = sportName;
        this.facility = facility;
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

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
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

