package com.example.SWP.dto.request.buyer;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateOrderRequest {

    @NotNull(message = "postId is required")
    Long postId;

    @NotNull(message = "deliveryMethod is required")
    DeliveryMethod deliveryMethod;

    @NotNull(message = "paymentMethod is required")
    PaymentMethod paymentMethod;

    @NotNull(message = "paymentType is required")
    PaymentType paymentType;
}
