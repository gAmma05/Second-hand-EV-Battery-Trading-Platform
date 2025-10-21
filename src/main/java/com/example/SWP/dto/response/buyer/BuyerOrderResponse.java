package com.example.SWP.dto.response.buyer;

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
public class BuyerOrderResponse {
    Long orderId;
    Long postId;
    String sellerName;
    PaymentMethod paymentMethod;
    PaymentType paymentType;
    OrderStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
