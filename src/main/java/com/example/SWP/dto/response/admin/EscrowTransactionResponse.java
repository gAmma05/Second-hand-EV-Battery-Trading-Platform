package com.example.SWP.dto.response.admin;

import com.example.SWP.enums.EscrowType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EscrowTransactionResponse {
    Long etId;
    Long escrowId;
    Long orderId;
    String receiverName;
    BigDecimal amount;
    EscrowType type;
    LocalDateTime createdAt;
}
