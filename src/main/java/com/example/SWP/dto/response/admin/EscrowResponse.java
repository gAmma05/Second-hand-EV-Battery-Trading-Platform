package com.example.SWP.dto.response.admin;

import com.example.SWP.enums.EscrowStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EscrowResponse {
    Long escrowId;
    Long orderId;
    String sellerName;
    String buyerName;
    BigDecimal depositAmount;
    BigDecimal paymentAmount;
    BigDecimal totalAmount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    EscrowStatus status;
}
