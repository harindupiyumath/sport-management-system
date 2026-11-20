package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.SlotDto;
import com.wayamba.sportms.dto.SportSlotsResponse;
import com.wayamba.sportms.exception.ResourceNotFoundException;
import com.wayamba.sportms.model.Booking;
import com.wayamba.sportms.model.BookingStatus;
import com.wayamba.sportms.repository.BookingRepository;
import com.wayamba.sportms.repository.SportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlotService {

    private final SportRepository sportRepository;
    private final BookingRepository bookingRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public SlotService(SportRepository sportRepository, BookingRepository bookingRepository) {
        this.sportRepository = sportRepository;
        this.bookingRepository = bookingRepository;
    }

    public SportSlotsResponse getSlotsForSportAndDate(Long sportId, LocalDate date) {
        sportRepository.findByIdAndActiveTrue(sportId)
                .orElseThrow(() -> new ResourceNotFoundException("Sport not found with id " + sportId));

        List<Booking> bookings = bookingRepository.findBySport_IdAndBookingDate(sportId, date);

        List<SlotDto> slots = new ArrayList<>();
        // 12 slots: 07:00 to 19:00
        for (int hour = 7; hour < 19; hour++) {
            LocalTime start = LocalTime.of(hour, 0);
            LocalTime end = LocalTime.of(hour + 1, 0);

            // Determine slot status:
            // BOOKED if any APPROVED booking exists
            // PENDING if any PENDING booking exists (and no APPROVED)
            // AVAILABLE otherwise
            boolean hasApproved = bookings.stream().anyMatch(b ->
                    b.getStartTime().equals(start) && b.getStatus() == BookingStatus.APPROVED);

            boolean hasPending = bookings.stream().anyMatch(b ->
                    b.getStartTime().equals(start) && b.getStatus() == BookingStatus.PENDING);

            String status = "AVAILABLE";
            if (hasApproved) {
                status = "BOOKED";
            } else if (hasPending) {
                status = "PENDING";
            }

            slots.add(new SlotDto(start.format(TIME_FORMATTER), end.format(TIME_FORMATTER), status));
        }

        return new SportSlotsResponse(sportId, date.toString(), slots);
    }
}

