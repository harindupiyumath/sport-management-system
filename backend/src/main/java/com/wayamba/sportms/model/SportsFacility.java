package com.wayamba.sportms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "facilities")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "facility_type", discriminatorType = DiscriminatorType.STRING)
public abstract class SportsFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    public SportsFacility() {
    }

    public SportsFacility(String name) {
        this.name = name;
    }

    public abstract FacilityType getFacilityType();

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
}

