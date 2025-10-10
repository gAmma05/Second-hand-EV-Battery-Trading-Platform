package com.example.SWP.entity;

import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import com.example.SWP.enums.SellerPlan;
import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
=======
import lombok.*;
import lombok.experimental.FieldDefaults;
>>>>>>> origin/locbe

import java.time.LocalDateTime;


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
<<<<<<< HEAD
    private String storeName;
    private String storeDescription;
    private String socialMedia;
    boolean status;
=======
    String storeName;
    String storeDescription;
    String socialMedia;
    int remainingPosts;
>>>>>>> origin/locbe

    LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    AuthProvider provider;
    @Enumerated(EnumType.STRING)
    Role role;
    @Enumerated(EnumType.STRING)
    SellerPlan sellerPlan;
    boolean enabled;
    boolean status;
}
