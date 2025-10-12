package com.example.SWP.entity;

import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "package_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String orderId;
    String transactionId;
    Long amount;

    @Enumerated(EnumType.STRING)
    PaymentMethod method;

    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "package_id")
    Package packageBought;
}
