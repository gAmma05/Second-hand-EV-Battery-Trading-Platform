package com.example.SWP.dto.request.buyer;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateOrderRequest {
    Long postId;
    DeliveryMethod deliveryMethod;
    PaymentMethod paymentMethod;
    PaymentType paymentType;
}
