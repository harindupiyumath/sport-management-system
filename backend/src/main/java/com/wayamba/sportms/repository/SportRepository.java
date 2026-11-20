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
    Optional<Sport> findByNameIgnoreCase(String name);
    Optional<Sport> findByIdAndActiveTrue(Long id);
    List<Sport> findAllByActiveTrueOrderByNameAsc();
    List<Sport> findAllByOrderByNameAsc();

    @Query("SELECT s FROM Sport s WHERE s.active = true AND TYPE(s.facility) = IndoorCourt ORDER BY s.name")
    List<Sport> findIndoorSports();

    @Query("SELECT s FROM Sport s WHERE s.active = true AND TYPE(s.facility) = OutdoorGround ORDER BY s.name")
    List<Sport> findOutdoorSports();
}

