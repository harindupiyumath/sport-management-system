package com.wayamba.sportms.dto;

public class AdminSportResponse {

    private Long id;
    private String name;
    private String facility;
    private String type;
    private boolean active;

    public AdminSportResponse(Long id, String name, String facility, String type, boolean active) {
        this.id = id;
        this.name = name;
        this.facility = facility;
        this.type = type;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFacility() {
        return facility;
    }

    public String getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }
}
