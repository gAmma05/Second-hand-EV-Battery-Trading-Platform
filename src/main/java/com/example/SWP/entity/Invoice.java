package com.example.SWP.entity;

import com.example.SWP.enums.InvoiceStatus;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invoices")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    Contract contract;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    String invoiceNumber;

    BigDecimal totalPrice;

    LocalDateTime createdAt;

    LocalDateTime dueDate;

    LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    InvoiceStatus status;
}
