package com.example.SWP.entity;

import com.example.SWP.enums.InvoiceStatus;
import com.example.SWP.enums.PaymentMethod;
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
@Entity
@Table(name = "invoices")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    Contract contract;

    @Column(nullable = false)
    String invoiceNumber;

    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    BigDecimal totalPrice;

    String currency;

    LocalDateTime createdAt;

    LocalDateTime dueDate;

    LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    InvoiceStatus status;
}
