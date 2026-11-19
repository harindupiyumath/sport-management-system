package com.wayamba.sportms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("LECTURER")
public class Lecturer extends User {

    public Lecturer() {
        super();
    }

    public Lecturer(String userId, String faculty, String passwordHash) {
        super(userId, faculty, passwordHash);
    }

    @Override
    public String getUserType() {
        return "LECTURER";
    }

    @Override
    public int getBookingPriority() {
        return 2;
    }
}

