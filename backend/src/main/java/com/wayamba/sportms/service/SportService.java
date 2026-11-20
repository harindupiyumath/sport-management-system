package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.AdminSportRequest;
import com.wayamba.sportms.dto.AdminSportResponse;
import com.wayamba.sportms.dto.SportResponse;
import com.wayamba.sportms.exception.ResourceNotFoundException;
import com.wayamba.sportms.model.Sport;
import com.wayamba.sportms.model.SportsFacility;
import com.wayamba.sportms.repository.FacilityRepository;
import com.wayamba.sportms.repository.SportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SportService {

    private final SportRepository sportRepository;
    private final FacilityRepository facilityRepository;

    public SportService(SportRepository sportRepository, FacilityRepository facilityRepository) {
        this.sportRepository = sportRepository;
        this.facilityRepository = facilityRepository;
    }

    public List<SportResponse> getSports(String type) {
        List<Sport> sports;
        if (type != null && type.equalsIgnoreCase("INDOOR")) {
            sports = sportRepository.findIndoorSports();
        } else if (type != null && type.equalsIgnoreCase("OUTDOOR")) {
            sports = sportRepository.findOutdoorSports();
        } else {
            sports = sportRepository.findAllByActiveTrueOrderByNameAsc();
        }

        return sports.stream().map(this::toPublicResponse).collect(Collectors.toList());
    }

    public List<AdminSportResponse> getAdminSports() {
        return sportRepository.findAllByOrderByNameAsc().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminSportResponse addSport(AdminSportRequest request) {
        String name = request.getName().trim();
        if (sportRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new IllegalStateException("A sport with this name already exists or was removed. Restore it from the sports list.");
        }

        SportsFacility facility = facilityRepository.findAll().stream()
                .filter(candidate -> candidate.getFacilityType() == request.getType())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No facility is configured for type " + request.getType()));

        Sport sport = sportRepository.save(new Sport(name, facility));
        return toAdminResponse(sport);
    }

    @Transactional
    public void removeSport(Long id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport not found with id " + id));
        sport.setActive(false);
        sportRepository.save(sport);
    }

    @Transactional
    public AdminSportResponse restoreSport(Long id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport not found with id " + id));
        sport.setActive(true);
        return toAdminResponse(sportRepository.save(sport));
    }

    private SportResponse toPublicResponse(Sport sport) {
        return new SportResponse(
                sport.getId(),
                sport.getName(),
                sport.getFacility().getName(),
                sport.getFacility().getFacilityType().name()
        );
    }

    private AdminSportResponse toAdminResponse(Sport sport) {
        return new AdminSportResponse(
                sport.getId(),
                sport.getName(),
                sport.getFacility().getName(),
                sport.getFacility().getFacilityType().name(),
                sport.isActive()
        );
    }
}

