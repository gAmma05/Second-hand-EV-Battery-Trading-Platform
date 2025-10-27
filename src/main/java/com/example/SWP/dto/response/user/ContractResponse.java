package com.example.SWP.dto.response.user;

import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.DeliveryMethod;
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
    Long id;
    Long orderId;
    String contractCode;
    String title;
    String content;
    BigDecimal price;
    String currency;
    boolean sellerSigned;
    boolean buyerSigned;
    LocalDateTime sellerSignedAt;
    LocalDateTime buyerSignedAt;
    ContractStatus status;
}
