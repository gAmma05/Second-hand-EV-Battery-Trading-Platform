package com.example.SWP.entity;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    Post post;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    User buyer;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    DeliveryMethod deliveryMethod;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    PaymentType paymentType;

    Integer serviceTypeId;

    BigDecimal shippingFee;

    BigDecimal depositPercentage;

    boolean depositPaid;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    OrderStatus status;

    LocalDateTime createdAt;
}
