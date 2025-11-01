package com.example.SWP.dto.response.user;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.ProductType;
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
public class OrderResponse {
    Long id;
    ProductType productType;

    String vehicleBrand;
    String model;

    String batteryBrand;
    String batteryType;

    DeliveryMethod deliveryMethod;
    PaymentType paymentType;

    BigDecimal price;
    BigDecimal shippingFee;

    OrderStatus status;

    LocalDateTime createdAt;
}
