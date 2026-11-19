package com.wayamba.sportms.repository;

import com.wayamba.sportms.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    Optional<Sport> findByName(String name);

    @Query("SELECT s FROM Sport s WHERE TYPE(s.facility) = IndoorCourt")
    List<Sport> findIndoorSports();

    @Query("SELECT s FROM Sport s WHERE TYPE(s.facility) = OutdoorGround")
    List<Sport> findOutdoorSports();
}

