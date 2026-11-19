package com.wayamba.sportms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String userId, String faculty, String passwordHash) {
        super(userId, faculty, passwordHash);
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }

    @Override
    public int getBookingPriority() {
        return 999;
    }
}

