package com.example.SWP.entity.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Objects;

@Data

@NoArgsConstructor

@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class UserNotificationKey implements Serializable {

    Long userId;

    Long notificationId;

    public UserNotificationKey(Long userId, Long notificationId) {
        this.userId = userId;
        this.notificationId = notificationId;
    }

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
