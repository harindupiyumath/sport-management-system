package com.wayamba.sportms.dto;

public class SportResponse {
    private Long id;
    private String name;
    private String facility;
    private String type;

    public SportResponse() {
    }

    public SportResponse(Long id, String name, String facility, String type) {
        this.id = id;
        this.name = name;
        this.facility = facility;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

