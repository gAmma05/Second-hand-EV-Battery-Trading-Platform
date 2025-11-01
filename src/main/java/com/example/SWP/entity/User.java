package com.example.SWP.entity;

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
import java.util.List;
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

    @Column(columnDefinition = "NVARCHAR(255)")
    String fullName;

    @Column(columnDefinition = "NVARCHAR(255)")
    String streetAddress;
    Integer provinceId;
    Integer districtId;

    @Column(columnDefinition = "VARCHAR(255)")
    String wardCode;
    @Column(columnDefinition = "NVARCHAR(255)")
    String address;

    @Column(columnDefinition = "VARCHAR(50)")
    String phone;

    @Column(columnDefinition = "VARCHAR(500)")
    String avatar;

    @Column(columnDefinition = "NVARCHAR(255)")
    String storeName;

    @Column(columnDefinition = "NVARCHAR(255)")
    String storeDescription;

    @Column(columnDefinition = "VARCHAR(255)")
    String socialMedia;

    int remainingBasicPosts;
    int remainingPremiumPosts;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    Role role;

    boolean status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<UserNotification> userNotifications;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    Wallet wallet;
}
