package com.wayamba.sportms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OUTDOOR")
public class OutdoorGround extends SportsFacility {

    public OutdoorGround() {
        super();
    }

    public OutdoorGround(String name) {
        super(name);
    }

    @Override
    public FacilityType getFacilityType() {
        return FacilityType.OUTDOOR;
    }
}

