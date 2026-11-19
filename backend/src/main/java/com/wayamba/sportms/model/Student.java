package com.wayamba.sportms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    public Student() {
        super();
    }

    public Student(String userId, String faculty, String passwordHash) {
        super(userId, faculty, passwordHash);
    }

    @Override
    public String getUserType() {
        return "STUDENT";
    }

    @Override
    public int getBookingPriority() {
        return 1;
    }
}

