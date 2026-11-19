package com.wayamba.sportms.repository;

import com.wayamba.sportms.model.Booking;
import com.wayamba.sportms.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);

    List<Booking> findAllByOrderByCreatedAtDesc();

    List<Booking> findBySport_IdAndBookingDate(Long sportId, LocalDate bookingDate);

    List<Booking> findBySport_IdAndBookingDateAndStartTimeAndStatus(
            Long sportId, LocalDate bookingDate, LocalTime startTime, BookingStatus status);

    boolean existsBySport_IdAndBookingDateAndStartTimeAndStatus(
            Long sportId, LocalDate bookingDate, LocalTime startTime, BookingStatus status);

    boolean existsByUser_UserIdAndSport_IdAndBookingDateAndStartTimeAndStatusIn(
            String userId, Long sportId, LocalDate bookingDate, LocalTime startTime, List<BookingStatus> statuses);
}

