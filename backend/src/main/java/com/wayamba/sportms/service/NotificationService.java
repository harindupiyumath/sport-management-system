package com.wayamba.sportms.service;

import com.wayamba.sportms.model.Notification;
import com.wayamba.sportms.model.User;
import com.wayamba.sportms.repository.NotificationRepository;
import com.wayamba.sportms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotification(User user, String message) {
        Notification notification = new Notification(user, message);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotification(String userId, String message) {
        userRepository.findByUserId(userId).ifPresent(user -> {
            createNotification(user, message);
        });
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(String userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUser_UserId(notificationId, userId)
                .orElseThrow(() -> new com.wayamba.sportms.exception.ResourceNotFoundException("Notification not found"));
        notification.markAsRead();
        notificationRepository.save(notification);
    }
}

