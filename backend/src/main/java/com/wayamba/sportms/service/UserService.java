package com.wayamba.sportms.service;

import com.wayamba.sportms.dto.UserProfileResponse;
import com.wayamba.sportms.exception.ResourceNotFoundException;
import com.wayamba.sportms.model.User;
import com.wayamba.sportms.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return new UserProfileResponse(user.getUserId(), user.getUserType(), user.getFaculty(), user.getCreatedAt());
    }
}

