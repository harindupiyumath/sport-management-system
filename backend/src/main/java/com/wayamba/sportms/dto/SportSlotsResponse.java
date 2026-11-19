package com.wayamba.sportms.dto;

import java.util.List;

public class SportSlotsResponse {
    private Long sportId;
    private String date;
    private List<SlotDto> slots;

    public SportSlotsResponse() {
    }

    public SportSlotsResponse(Long sportId, String date, List<SlotDto> slots) {
        this.sportId = sportId;
        this.date = date;
        this.slots = slots;
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

    public List<SlotDto> getSlots() {
        return slots;
    }

    public void setSlots(List<SlotDto> slots) {
        this.slots = slots;
    }
}

