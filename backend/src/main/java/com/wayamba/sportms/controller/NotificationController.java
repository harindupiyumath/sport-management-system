package com.wayamba.sportms.controller;

import com.wayamba.sportms.dto.NotificationResponse;
import com.wayamba.sportms.dto.SimpleMessageResponse;
import com.wayamba.sportms.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<NotificationResponse> list = notificationService.getUserNotifications(userId).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<SimpleMessageResponse> markAsRead(@PathVariable Long id, Authentication authentication) {
        String userId = authentication.getName();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(new SimpleMessageResponse("Marked as read"));
    }
}

