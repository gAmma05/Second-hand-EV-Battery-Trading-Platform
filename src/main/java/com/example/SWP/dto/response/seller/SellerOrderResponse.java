package com.example.SWP.dto.response.seller;

import com.example.SWP.dto.response.OrderDeliveryStatusResponse;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerOrderResponse {
    Long orderId;
    Long postId;
    String buyerName;
    PaymentType paymentType;
    DeliveryMethod deliveryMethod;
    OrderStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}
