package com.example.SWP.entity.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotificationKey implements Serializable {

<<<<<<< HEAD
    Long userId;

=======
    @Column(name = "user_id")
    Long userId;

    @Column(name = "notification_id")
>>>>>>> main
    Long notificationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserNotificationKey)) return false;
        UserNotificationKey that = (UserNotificationKey) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, notificationId);
    }
}

