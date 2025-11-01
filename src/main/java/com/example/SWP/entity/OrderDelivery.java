package com.example.SWP.entity;

import com.example.SWP.enums.DeliveryProvider;
import com.example.SWP.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_delivery_status")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class OrderDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)")
    DeliveryProvider deliveryProvider;

    @Column(columnDefinition = "NVARCHAR(255)")
    String deliveryTrackingNumber;

    LocalDateTime deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    DeliveryStatus status;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
