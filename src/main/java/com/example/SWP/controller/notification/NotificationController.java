package com.example.SWP.controller.notification;

import com.example.SWP.entity.notification.Notification;
import com.example.SWP.service.notification.NotificationService;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/notification")
@NoArgsConstructor
public class NotificationController {
    NotificationService notificationService;

    @GetMapping("/{role}")
    public ResponseEntity<?> getNotificationById(@PathVariable String role, @RequestParam Long id) {
        Set<Notification> notificationList = notificationService.getNotificationList(id);
        return ResponseEntity
                .status(200)
                .body(notificationList);
    }

}
