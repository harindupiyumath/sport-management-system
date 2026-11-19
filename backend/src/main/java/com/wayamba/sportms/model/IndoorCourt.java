package com.wayamba.sportms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INDOOR")
public class IndoorCourt extends SportsFacility {

    public IndoorCourt() {
        super();
    }

    public IndoorCourt(String name) {
        super(name);
    }

    @Override
    public FacilityType getFacilityType() {
        return FacilityType.INDOOR;
    }
}

