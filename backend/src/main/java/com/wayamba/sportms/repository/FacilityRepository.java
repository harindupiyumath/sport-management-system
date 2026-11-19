package com.wayamba.sportms.repository;

import com.wayamba.sportms.model.SportsFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<SportsFacility, Long> {
    Optional<SportsFacility> findByName(String name);
}

