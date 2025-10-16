package com.example.SWP.entity.wallet;

import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    @JsonIgnore
    Wallet wallet;

    String orderId;
    String bankCode;
    BigDecimal amount;
    String description;

    @Enumerated(EnumType.STRING)
    TransactionType type;

    @Enumerated(EnumType.STRING)
    PaymentStatus status; // PENDING, SUCCESS, FAILED

    BigDecimal balanceBefore;
    BigDecimal balanceAfter;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
