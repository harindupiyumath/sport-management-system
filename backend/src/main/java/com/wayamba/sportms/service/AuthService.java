package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.*;
import com.wayamba.sportms.exception.InvalidCredentialsException;
import com.wayamba.sportms.exception.ResourceNotFoundException;
import com.wayamba.sportms.exception.UserAlreadyExistsException;
import com.wayamba.sportms.exception.ValidationException;
import com.wayamba.sportms.model.*;
import com.wayamba.sportms.repository.UserRepository;
import com.wayamba.sportms.util.JwtUtil;
import com.wayamba.sportms.util.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public SimpleMessageResponse signup(SignupRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        if (!PasswordValidator.isValid(req.getPassword())) {
            throw new ValidationException("Password does not meet complexity requirements");
        }

        if (userRepository.existsByUserId(req.getUserId())) {
            throw new UserAlreadyExistsException("User ID already exists");
        }

        // Validate faculty
        try {
            Faculty.valueOf(req.getFaculty().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidationException("Invalid faculty: must be APPLIED_SCIENCES, BUSINESS_STUDIES, or TECHNOLOGY");
        }

        String role = req.getRole().toUpperCase();
        String encodedPassword = passwordEncoder.encode(req.getPassword());
        User user;

        if ("STUDENT".equals(role)) {
            user = new Student(req.getUserId(), req.getFaculty().toUpperCase(), encodedPassword);
        } else if ("LECTURER".equals(role)) {
            user = new Lecturer(req.getUserId(), req.getFaculty().toUpperCase(), encodedPassword);
        } else {
            throw new ValidationException("Invalid role: must be STUDENT or LECTURER");
        }

        userRepository.save(user);

        return new SimpleMessageResponse("Successfully signed up. Hi " + user.getUserId() + "!", user.getUserId());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid User ID or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid User ID or password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUserType());
        UserDto userDto = new UserDto(user.getUserId(), user.getUserType(), user.getFaculty());

        return new LoginResponse(token, userDto);
    }

    @Transactional
    public SimpleMessageResponse forgotPassword(ForgotPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        if (!PasswordValidator.isValid(req.getNewPassword())) {
            throw new ValidationException("Password does not meet complexity requirements");
        }

        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return new SimpleMessageResponse("Password successfully changed");
    }
}

