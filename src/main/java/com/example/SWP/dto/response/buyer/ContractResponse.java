package com.example.SWP.dto.response.buyer;

import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.PaymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractResponse {
    Long contractId;
    Long orderId;
    String contractCode;
    String title;
    String content;
    BigDecimal price;
    String currency;
    boolean sellerSigned;
    LocalDateTime sellerSignedAt;
    boolean buyerSigned;
    LocalDateTime buyerSignedAt;
    ContractStatus status;
    PaymentType paymentType;
}
