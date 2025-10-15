package com.example.SWP.entity.notification;

import com.example.SWP.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_notification")
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserNotification {

    @EmbeddedId
    UserNotificationKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @MapsId("notificationId")
    @JoinColumn(name = "notification_id")
    Notification notification;

    @Column(name = "is_read")
    boolean isRead;

    @Column(name = "received_at")
    LocalDateTime receivedAt = LocalDateTime.now();

    public UserNotification(User user, Notification notification) {
        this.user = user;
        this.notification = notification;
        this.id = new UserNotificationKey(user.getId(), notification.getId());
    }
}
