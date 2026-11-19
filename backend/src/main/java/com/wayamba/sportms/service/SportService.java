package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.SportResponse;
import com.wayamba.sportms.model.Sport;
import com.wayamba.sportms.repository.SportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SportService {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    public List<SportResponse> getSports(String type) {
        List<Sport> sports;
        if (type != null && type.equalsIgnoreCase("INDOOR")) {
            sports = sportRepository.findIndoorSports();
        } else if (type != null && type.equalsIgnoreCase("OUTDOOR")) {
            sports = sportRepository.findOutdoorSports();
        } else {
            sports = sportRepository.findAll();
        }

        return sports.stream().map(s -> new SportResponse(
                s.getId(),
                s.getName(),
                s.getFacility().getName(),
                s.getFacility().getFacilityType().name()
        )).collect(Collectors.toList());
    }
}

