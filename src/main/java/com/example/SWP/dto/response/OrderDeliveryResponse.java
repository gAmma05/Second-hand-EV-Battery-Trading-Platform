package com.example.SWP.dto.response;

import com.example.SWP.enums.DeliveryProvider;
import com.example.SWP.enums.DeliveryStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderDeliveryResponse {
    Long id;
    String orderId;
    DeliveryProvider deliveryProvider;
    String deliveryTrackingNumber;
    LocalDateTime deliveryDate;
    DeliveryStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
