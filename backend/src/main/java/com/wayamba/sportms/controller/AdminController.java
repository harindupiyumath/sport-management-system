package com.wayamba.sportms.controller;

import com.wayamba.sportms.dto.AdminBookingResponse;
import com.wayamba.sportms.dto.SignupRequest;
import com.wayamba.sportms.dto.SimpleMessageResponse;
import com.wayamba.sportms.dto.UserDto;
import com.wayamba.sportms.service.AdminService;
import com.wayamba.sportms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
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

    /** Admin creates a Student, Lecturer or Admin account (user must change password at first login). */
    @PostMapping("/users")
    public ResponseEntity<SimpleMessageResponse> createUser(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAccountByAdmin(req));
    }
}

