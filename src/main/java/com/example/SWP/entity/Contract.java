package com.example.SWP.entity;

import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "contracts")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    String contractCode;

    @Column(columnDefinition = "NVARCHAR(1000)")
    String content;

    BigDecimal price;

    boolean sellerSigned;

    boolean buyerSigned;

    LocalDateTime sellerSignedAt;

    LocalDateTime buyerSignedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    ContractStatus status;
}
