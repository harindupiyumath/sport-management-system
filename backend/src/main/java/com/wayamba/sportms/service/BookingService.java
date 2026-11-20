package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.BookingRequest;
import com.wayamba.sportms.dto.BookingResponse;
import com.wayamba.sportms.exception.DuplicateBookingException;
import com.wayamba.sportms.exception.ResourceNotFoundException;
import com.wayamba.sportms.exception.SlotUnavailableException;
import com.wayamba.sportms.exception.ValidationException;
import com.wayamba.sportms.model.*;
import com.wayamba.sportms.repository.BookingRepository;
import com.wayamba.sportms.repository.SportRepository;
import com.wayamba.sportms.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
                          SportRepository sportRepository, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookingResponse createBooking(String userId, BookingRequest req) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Sport sport = sportRepository.findByIdAndActiveTrue(req.getSportId())
                .orElseThrow(() -> new ResourceNotFoundException("Sport not found with id: " + req.getSportId()));

        LocalDate bookingDate;
        LocalTime startTime;
        LocalTime endTime;
        try {
            bookingDate = LocalDate.parse(req.getDate());
            startTime = LocalTime.parse(req.getStart());
            endTime = LocalTime.parse(req.getEnd());
        } catch (Exception e) {
            throw new ValidationException("Invalid date or time format");
        }

        // Validate time slot: on the hour, within 07:00 - 19:00 and exactly 1 hour long.
        // (A slot like 07:30-08:30 would overlap two real slots without being detected as a clash.)
        if (startTime.getMinute() != 0 || startTime.getSecond() != 0 || startTime.getNano() != 0
                || startTime.isBefore(LocalTime.of(7, 0)) || endTime.isAfter(LocalTime.of(19, 0))
                || !startTime.plusHours(1).equals(endTime)) {
            throw new ValidationException("Booking must be a single 1-hour slot (on the hour) between 07:00 and 19:00");
        }

        // Reject bookings in the past (including slots earlier today that have already started)
        if (bookingDate.isBefore(LocalDate.now())
                || (bookingDate.equals(LocalDate.now()) && !startTime.isAfter(LocalTime.now()))) {
            throw new ValidationException("Cannot book a time slot in the past");
        }

        // Check if slot is already APPROVED (BOOKED)
        boolean isApproved = bookingRepository.existsBySport_IdAndBookingDateAndStartTimeAndStatus(
                sport.getId(), bookingDate, startTime, BookingStatus.APPROVED);
        if (isApproved) {
            throw new SlotUnavailableException("Time slot is already booked and approved");
        }

        // Check duplicate booking by the same user for the same slot (PENDING or APPROVED)
        boolean duplicate = bookingRepository.existsByUser_UserIdAndSport_IdAndBookingDateAndStartTimeAndStatusIn(
                userId, sport.getId(), bookingDate, startTime, List.of(BookingStatus.PENDING, BookingStatus.APPROVED));
        if (duplicate) {
            throw new DuplicateBookingException("You already have an active booking request for this time slot");
        }

        // Lecturer Priority check:
        // If user is Lecturer (priority 2) and there are PENDING student bookings for this slot:
        List<Booking> pendingBookings = bookingRepository.findBySport_IdAndBookingDateAndStartTimeAndStatus(
                sport.getId(), bookingDate, startTime, BookingStatus.PENDING);

        if (!pendingBookings.isEmpty()) {
            if (user.getBookingPriority() > 1) { // Lecturer
                for (Booking pending : pendingBookings) {
                    if (pending.getUser().getBookingPriority() < user.getBookingPriority()) {
                        // Bump student
                        pending.reject();
                        bookingRepository.save(pending);
                        notificationService.createNotification(pending.getUser(),
                                "Your " + sport.getName() + " booking on " + bookingDate + " " + startTime.format(TIME_FORMATTER) +
                                        " was rejected (slot given to a lecturer).");
                    }
                }
            } else {
                // If a student tries to book a slot that already has a pending student booking
                throw new SlotUnavailableException("Time slot is currently pending approval for another user");
            }
        }

        Booking booking = new Booking(user, sport, bookingDate, startTime, endTime);
        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(user,
                "Booking request submitted for " + sport.getName() + " on " + bookingDate + " at " + startTime.format(TIME_FORMATTER) + " (Pending approval).");

        return toDto(saved);
    }

    public List<BookingResponse> getUserBookings(String userId) {
        return bookingRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(String userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        if (!booking.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalStateException("Booking is already " + booking.getStatus());
        }

        booking.cancel();
        bookingRepository.save(booking);

        notificationService.createNotification(booking.getUser(),
                "Your booking for " + booking.getSport().getName() + " on " + booking.getBookingDate() + " has been cancelled.");
    }

    public BookingResponse toDto(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getSport().getId(),
                b.getSport().getName(),
                b.getSport().getFacility().getName(),
                b.getBookingDate().toString(),
                b.getStartTime().format(TIME_FORMATTER),
                b.getEndTime().format(TIME_FORMATTER),
                b.getStatus().name()
        );
    }
}

