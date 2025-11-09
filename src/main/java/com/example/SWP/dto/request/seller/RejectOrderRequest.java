package com.example.SWP.dto.request.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RejectOrderRequest {
    @NotNull(message = "ID đơn hàng không được để trống")
    Long orderId;

    @NotBlank(message = "Lý do từ chối không được để trống")
    String reason;
}
