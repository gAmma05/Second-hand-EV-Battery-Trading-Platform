package com.example.SWP.controller.notification;

import com.example.SWP.entity.notification.Notification;
import com.example.SWP.entity.notification.UserNotification;
import com.example.SWP.service.notification.NotificationService;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("user/notification")
@NoArgsConstructor
public class NotificationController {
    NotificationService notificationService;

    @GetMapping("/unread_list")
    public ResponseEntity<?> getUnreadNotification(Authentication authentication) {
        Set<UserNotification> notificationList = notificationService.getUnreadNotifications(authentication);
        return ResponseEntity
                .status(200)
                .body(notificationList);
    }

}
