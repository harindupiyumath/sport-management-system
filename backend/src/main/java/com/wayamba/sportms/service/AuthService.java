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

    /** Public self-registration: students and lecturers only. */
    @Transactional
    public SimpleMessageResponse signup(SignupRequest req) {
        User user = createAccount(req, false, false);
        return new SimpleMessageResponse("Successfully signed up. Hi " + user.getUserId() + "!", user.getUserId());
    }

    /**
     * Account created by an admin (any role, including ADMIN).
     * The new user must choose their own password at first login.
     */
    @Transactional
    public SimpleMessageResponse createAccountByAdmin(SignupRequest req) {
        User user = createAccount(req, true, true);
        return new SimpleMessageResponse(
                user.getUserType() + " account '" + user.getUserId() + "' created. The user must change the password at first login.",
                user.getUserId());
    }

    private User createAccount(SignupRequest req, boolean allowAdmin, boolean mustChangePassword) {
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
        } else if ("ADMIN".equals(role) && allowAdmin) {
            user = new Admin(req.getUserId(), req.getFaculty().toUpperCase(), encodedPassword);
        } else {
            throw new ValidationException(allowAdmin
                    ? "Invalid role: must be STUDENT, LECTURER or ADMIN"
                    : "Invalid role: must be STUDENT or LECTURER");
        }

        user.setMustChangePassword(mustChangePassword);
        return userRepository.save(user);
    }

    /** Logged-in user changes their own password (requires the current one). */
    @Transactional
    public LoginResponse changePassword(String userId, ChangePasswordRequest req) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }
        if (!PasswordValidator.isValid(req.getNewPassword())) {
            throw new ValidationException("Password does not meet complexity requirements");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Fresh token without the "password change required" restriction
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserType(), false);
        return new LoginResponse(token, new UserDto(user.getUserId(), user.getUserType(), user.getFaculty()), false);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid User ID or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid User ID or password");
        }

        boolean mustChange = user.isMustChangePassword();
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserType(), mustChange);
        UserDto userDto = new UserDto(user.getUserId(), user.getUserType(), user.getFaculty());

        return new LoginResponse(token, userDto, mustChange);
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

        // This reset needs only a user ID, so it must never work for admin accounts
        // (otherwise anyone could take over the admin by typing "admin").
        if (user instanceof Admin) {
            throw new ValidationException(
                    "Admin passwords cannot be reset here. Log in and use Change Password instead.");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return new SimpleMessageResponse("Password successfully changed");
    }
}

