package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.AdminBookingResponse;
import com.wayamba.sportms.dto.UserDto;
import com.wayamba.sportms.exception.ResourceNotFoundException;
import com.wayamba.sportms.exception.SlotUnavailableException;
import com.wayamba.sportms.model.Booking;
import com.wayamba.sportms.model.BookingStatus;
import com.wayamba.sportms.repository.BookingRepository;
import com.wayamba.sportms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AdminService(BookingRepository bookingRepository, UserRepository userRepository,
                        NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<AdminBookingResponse> getBookings(String statusStr) {
        List<Booking> bookings;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                BookingStatus status = BookingStatus.valueOf(statusStr.toUpperCase());
                bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(status);
            } catch (IllegalArgumentException e) {
                bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
            }
        } else {
            bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        }

        return bookings.stream().map(b -> new AdminBookingResponse(
                b.getId(),
                b.getUser().getUserId(),
                b.getUser().getUserType(),
                b.getSport().getName(),
                b.getBookingDate().toString(),
                b.getStartTime().format(TIME_FORMATTER),
                b.getEndTime().format(TIME_FORMATTER),
                b.getStatus().name()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void approveBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + id));

        // Only pending requests can be approved (not cancelled / rejected / already approved ones)
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be approved (current status: " + booking.getStatus() + ")");
        }

        // Check if there is already an APPROVED booking for this sport/date/time
        boolean alreadyApproved = bookingRepository.existsBySport_IdAndBookingDateAndStartTimeAndStatus(
                booking.getSport().getId(), booking.getBookingDate(), booking.getStartTime(), BookingStatus.APPROVED);

        if (alreadyApproved) {
            throw new SlotUnavailableException("Time slot is already booked and approved for another user");
        }

        booking.approve();
        bookingRepository.save(booking);

        notificationService.createNotification(booking.getUser(),
                "Your " + booking.getSport().getName() + " booking on " + booking.getBookingDate() +
                        " " + booking.getStartTime().format(TIME_FORMATTER) + " was approved");

        // Any other requests still pending for the same slot can no longer be fulfilled - reject them
        List<Booking> competing = bookingRepository.findBySport_IdAndBookingDateAndStartTimeAndStatus(
                booking.getSport().getId(), booking.getBookingDate(), booking.getStartTime(), BookingStatus.PENDING);
        for (Booking other : competing) {
            if (other.getId().equals(booking.getId())) {
                continue;
            }
            other.reject();
            bookingRepository.save(other);
            notificationService.createNotification(other.getUser(),
                    "Your " + other.getSport().getName() + " booking on " + other.getBookingDate() +
                            " " + other.getStartTime().format(TIME_FORMATTER) + " was rejected (slot was given to another user)");
        }
    }

    @Transactional
    public void rejectBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + id));

        if (booking.getStatus() == BookingStatus.REJECTED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already " + booking.getStatus());
        }

        booking.reject();
        bookingRepository.save(booking);

        notificationService.createNotification(booking.getUser(),
                "Your " + booking.getSport().getName() + " booking on " + booking.getBookingDate() +
                        " " + booking.getStartTime().format(TIME_FORMATTER) + " was rejected");
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDto(u.getUserId(), u.getUserType(), u.getFaculty()))
                .collect(Collectors.toList());
    }
}

