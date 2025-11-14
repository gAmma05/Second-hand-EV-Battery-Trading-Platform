package com.example.SWP.entity.escrow;

import com.example.SWP.enums.EscrowType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "escrow_transactions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class EscrowTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "escrow_id", nullable = false)
    Escrow escrow;

    Long receiverId;

    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    EscrowType type;

    LocalDateTime createdAt;
}
