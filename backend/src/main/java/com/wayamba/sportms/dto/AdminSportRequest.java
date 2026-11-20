package com.wayamba.sportms.dto;

import com.wayamba.sportms.model.FacilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminSportRequest {

    @NotBlank(message = "Sport name is required")
    @Size(max = 100, message = "Sport name must be 100 characters or fewer")
    private String name;

    @NotNull(message = "Facility type is required")
    private FacilityType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FacilityType getType() {
        return type;
    }

    public void setType(FacilityType type) {
        this.type = type;
    }
}
