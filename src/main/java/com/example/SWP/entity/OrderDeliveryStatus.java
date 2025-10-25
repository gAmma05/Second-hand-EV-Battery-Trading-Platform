package com.example.SWP.entity;

import com.example.SWP.enums.DeliveryProvider;
import com.example.SWP.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_delivery_status")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class OrderDeliveryStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    DeliveryProvider deliveryProvider;
    String deliveryTrackingNumber;
    LocalDateTime deliveryDate;

    @Enumerated(EnumType.STRING)
    DeliveryStatus status;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
