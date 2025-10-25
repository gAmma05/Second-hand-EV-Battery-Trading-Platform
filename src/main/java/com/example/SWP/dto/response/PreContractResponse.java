package com.example.SWP.dto.response;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PreContractResponse {
    Long orderId;
    String title;
    BigDecimal price;
    PaymentType paymentType;
    DeliveryMethod deliveryMethod;
    String currency;
}
