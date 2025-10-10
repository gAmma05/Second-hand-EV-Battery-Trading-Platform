package com.example.SWP.entity;

import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private String storeName;
    private String storeDescription;
    private String socialMedia;
    boolean status;

    LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    AuthProvider provider;
    @Enumerated(EnumType.STRING)
    Role role;
    boolean enabled;
}
