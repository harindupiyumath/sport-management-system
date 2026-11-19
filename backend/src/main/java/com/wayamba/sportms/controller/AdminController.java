package com.wayamba.sportms.controller;

import com.wayamba.sportms.dto.AdminBookingResponse;
import com.wayamba.sportms.dto.SimpleMessageResponse;
import com.wayamba.sportms.dto.UserDto;
import com.wayamba.sportms.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getBookings(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getBookings(status));
    }

    @PutMapping("/bookings/{id}/approve")
    public ResponseEntity<SimpleMessageResponse> approveBooking(@PathVariable Long id) {
        adminService.approveBooking(id);
        return ResponseEntity.ok(new SimpleMessageResponse("Booking approved"));
    }

    @PutMapping("/bookings/{id}/reject")
    public ResponseEntity<SimpleMessageResponse> rejectBooking(@PathVariable Long id) {
        adminService.rejectBooking(id);
        return ResponseEntity.ok(new SimpleMessageResponse("Booking rejected"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }
}

