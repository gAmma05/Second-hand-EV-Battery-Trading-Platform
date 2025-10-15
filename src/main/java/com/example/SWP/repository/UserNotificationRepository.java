package com.example.SWP.repository;

import com.example.SWP.entity.notification.UserNotification;
import com.example.SWP.entity.notification.UserNotificationKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UserNotificationKey> {
    List<UserNotification> findByUserId(Long userId);
}
