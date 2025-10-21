package com.example.SWP.controller.notification;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.NotificationResponse;
import com.example.SWP.entity.notification.Notification;
import com.example.SWP.entity.notification.UserNotification;
import com.example.SWP.service.notification.NotificationService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("user/notification")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/unread-list")
    public ResponseEntity<?> getUnreadNotification(Authentication authentication) {
        Set<UserNotification> notificationList = notificationService.getUnreadNotifications(authentication);
        List<NotificationResponse> response = notificationList.stream()
                .map(un -> new NotificationResponse(
                        un.getNotification().getTitle(),
                        un.getNotification().getContent(),
                        un.getNotification().getCreatedAt()
                ))
                .toList();
        return ResponseEntity
                .status(200)
                .body(response);
    }

    @GetMapping("/mark-as-read/{notificationId}")
    public ResponseEntity<?> markAsRead(Authentication authentication, @PathVariable Long notificationId) {
        notificationService.markAsRead(authentication, notificationId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Marked as read successfully")
                        .build()
        );
    }

}
