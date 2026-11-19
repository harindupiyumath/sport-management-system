package com.wayamba.sportms.repository;

import com.wayamba.sportms.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(String userId);
    Optional<Notification> findByIdAndUser_UserId(Long id, String userId);
}

