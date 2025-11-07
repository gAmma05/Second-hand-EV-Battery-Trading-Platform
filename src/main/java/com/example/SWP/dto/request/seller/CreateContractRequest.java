package com.example.SWP.dto.request.seller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContractRequest {

    @NotNull(message = "ID đơn hàng là bắt buộc")
    Long orderId;

    @NotNull(message = "Nội dung của hợp đồng là bắt buộc")
    @Size(max = 1000, message = "Nội dung phải dưới 1000 ký tự")
    String content;

    BigDecimal shippingFee;
}
