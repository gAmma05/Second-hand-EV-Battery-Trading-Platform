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

    @NotNull(message = "ID của bài đăng là bắt buộc")
    Long postId;

    @NotNull(message = "Phương thức giao hàng là bắt buộc")
    DeliveryMethod deliveryMethod;


    @NotNull(message = "Phương thức thanh toán là bắt buộc")
    PaymentType paymentType;

    Integer serviceTypeId;
}
