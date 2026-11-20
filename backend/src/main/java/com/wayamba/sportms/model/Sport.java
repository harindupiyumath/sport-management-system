package com.wayamba.sportms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sports")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id", nullable = false)
    private SportsFacility facility;

    public Sport() {
    }

    public Sport(String name, SportsFacility facility) {
        this.name = name;
        this.facility = facility;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public SportsFacility getFacility() {
        return facility;
    }

    public void setFacility(SportsFacility facility) {
        this.facility = facility;
    }
}

