package com.example.SWP.dto.request.seller;

import com.example.SWP.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusRequest {
    @NotBlank(message = "OrderId is required")
    Long orderId;

    @NotBlank(message = "Status is required")
    OrderStatus status;
}
