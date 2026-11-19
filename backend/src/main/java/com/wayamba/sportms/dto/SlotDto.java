package com.wayamba.sportms.dto;


public class SlotDto {
    private String start;
    private String end;
    private String status;

    public SlotDto() {
    }

    public SlotDto(String start, String end, String status) {
        this.start = start;
        this.end = end;
        this.status = status;
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

