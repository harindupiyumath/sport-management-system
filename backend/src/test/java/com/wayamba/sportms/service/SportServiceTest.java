package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.AdminSportRequest;
import com.wayamba.sportms.dto.AdminSportResponse;
import com.wayamba.sportms.model.FacilityType;
import com.wayamba.sportms.model.IndoorCourt;
import com.wayamba.sportms.model.Sport;
import com.wayamba.sportms.repository.FacilityRepository;
import com.wayamba.sportms.repository.SportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SportServiceTest {

    @Mock
    private SportRepository sportRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private SportService sportService;

    @Test
    void addSportTrimsNameAndAssociatesSelectedFacilityType() {
        IndoorCourt facility = new IndoorCourt("Indoor Court");
        AdminSportRequest request = new AdminSportRequest();
        request.setName("  Futsal  ");
        request.setType(FacilityType.INDOOR);

        when(sportRepository.findByNameIgnoreCase("Futsal")).thenReturn(Optional.empty());
        when(facilityRepository.findAll()).thenReturn(List.of(facility));
        when(sportRepository.save(any(Sport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminSportResponse result = sportService.addSport(request);

        assertEquals("Futsal", result.getName());
        assertEquals("INDOOR", result.getType());
        assertTrue(result.isActive());
    }

    @Test
    void addSportRejectsNamesAlreadyInUseOrPreviouslyRemoved() {
        AdminSportRequest request = new AdminSportRequest();
        request.setName("Badminton");
        request.setType(FacilityType.INDOOR);
        when(sportRepository.findByNameIgnoreCase("Badminton"))
                .thenReturn(Optional.of(new Sport("Badminton", new IndoorCourt("Indoor Court"))));

        assertThrows(IllegalStateException.class, () -> sportService.addSport(request));
    }

    @Test
    void removeSportHidesSportWithoutDeletingItsBookingReference() {
        Sport sport = new Sport("Badminton", new IndoorCourt("Indoor Court"));
        when(sportRepository.findById(1L)).thenReturn(Optional.of(sport));
        when(sportRepository.save(sport)).thenReturn(sport);

        sportService.removeSport(1L);

        assertFalse(sport.isActive());
        verify(sportRepository).save(sport);
        verify(sportRepository, never()).delete(any(Sport.class));
    }

    @Test
    void restoreSportMakesItAvailableAgain() {
        Sport sport = new Sport("Badminton", new IndoorCourt("Indoor Court"));
        sport.setActive(false);
        when(sportRepository.findById(1L)).thenReturn(Optional.of(sport));
        when(sportRepository.save(sport)).thenReturn(sport);

        AdminSportResponse result = sportService.restoreSport(1L);

        assertTrue(result.isActive());
        assertTrue(sport.isActive());
    }
}
