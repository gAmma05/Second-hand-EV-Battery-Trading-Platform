package com.example.SWP.dto.request.buyer;

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

    @NotNull(message = "orderId is required")
    Long orderId;

    @NotNull(message = "If you want to cancel it without reason, please write 'None'")
    String reason;
}
