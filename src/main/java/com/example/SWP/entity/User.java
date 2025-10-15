package com.example.SWP.entity;

import com.example.SWP.entity.notification.Notification;
import com.example.SWP.entity.notification.UserNotification;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;


@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String email;
    @Column(unique = true)
    String googleId;
    String password;
    String fullName;
    String address;
    String phone;
    String avatar;
    String storeName;
    String storeDescription;
    String socialMedia;
    int remainingPosts;

    LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    AuthProvider provider;
    @Enumerated(EnumType.STRING)
    Role role;
    Long sellerPackageId;
    LocalDateTime planExpiry;
    boolean enabled;

    boolean status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserNotification> userNotifications;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    Wallet wallet;
}
