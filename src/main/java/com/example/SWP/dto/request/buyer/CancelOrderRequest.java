package com.example.SWP.dto.request.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelOrderRequest {

    @NotNull(message = "Vui lòng cung cấp ID của đơn hàng cần hủy")
    Long orderId;

    @NotBlank(message = "Vui lòng nhập lý do hủy đơn hàng")
    String reason;
}
