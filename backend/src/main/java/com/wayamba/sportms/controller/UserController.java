package com.wayamba.sportms.controller;

import com.wayamba.sportms.dto.ChangePasswordRequest;
import com.wayamba.sportms.dto.LoginResponse;
import com.wayamba.sportms.dto.UserProfileResponse;
import com.wayamba.sportms.service.AuthService;
import com.wayamba.sportms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /** Change own password; returns a fresh token. Also used for the forced first-login change. */
    @PutMapping("/me/password")
    public ResponseEntity<LoginResponse> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                                       Authentication authentication) {
        return ResponseEntity.ok(authService.changePassword(authentication.getName(), req));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }
}

