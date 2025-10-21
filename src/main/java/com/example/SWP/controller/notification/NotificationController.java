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
        List<NotificationResponse> responses = notificationService.getUnreadNotifications(authentication);
        if (responses == null || responses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<NotificationResponse>>builder()
                            .success(false)
                            .message("No unread notifications")
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<NotificationResponse>>builder()
                        .success(true)
                        .message("List of unread notifications retrieved successfully")
                        .data(responses)
                        .build()
        );
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
