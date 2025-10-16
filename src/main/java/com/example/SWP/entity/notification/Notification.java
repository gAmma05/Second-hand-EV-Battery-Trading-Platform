package com.example.SWP.entity.notification;

import com.example.SWP.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "notification")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;
    String content;
    LocalDateTime createdAt = LocalDateTime.now();


    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserNotification> userNotifications;
}

