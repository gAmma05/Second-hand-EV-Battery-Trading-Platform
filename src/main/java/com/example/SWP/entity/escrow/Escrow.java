package com.example.SWP.entity.escrow;

import com.example.SWP.entity.Order;
import com.example.SWP.enums.EscrowStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "escrow")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Escrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    Order order;

    @Enumerated(EnumType.STRING)
    EscrowStatus status;

    Long buyerId;
    Long sellerId;

    BigDecimal depositAmount;
    BigDecimal paymentAmount;

    BigDecimal totalAmount;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}