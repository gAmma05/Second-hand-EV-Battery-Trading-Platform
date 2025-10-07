package com.example.SWP.entity;

import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Enumerated(EnumType.STRING)
    AuthProvider provider;

    @Enumerated(EnumType.STRING)
    Role role;
    boolean enabled;
}
