package com.example.SWP.entity;

import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "contracts")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Column(unique = true, nullable = false)
    String contractCode;

    String title;

    @Column(columnDefinition = "TEXT")
    String content;

    double price;

    String currency;

    boolean sellerSigned;

    boolean buyerSigned;

    LocalDateTime sellerSignedAt;

    LocalDateTime buyerSignedAt;

    @Enumerated(EnumType.STRING)
    ContractStatus status;

    @Enumerated(EnumType.STRING)
    PaymentType paymentType;
}
