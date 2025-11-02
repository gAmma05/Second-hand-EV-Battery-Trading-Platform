package com.example.SWP.dto.response.user;

import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.DeliveryMethod;
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
public class ContractResponse {
    Long id;
    String contractCode;
    Long orderId;

    String buyerName;
    String buyerAddress;
    String buyerPhone;
    String sellerName;
    String sellerAddress;
    String sellerPhone;

    ProductType productType;

    String vehicleBrand;
    String model;
    Integer yearOfManufacture;
    String color;
    Integer mileage;

    String batteryType;
    Integer capacity;
    String voltage;
    String batteryBrand;

    Integer weight;

    DeliveryMethod deliveryMethod;
    PaymentType paymentType;

    BigDecimal price;
    BigDecimal shippingFee;
    BigDecimal totalFee;

    BigDecimal depositPercentage;

    boolean sellerSigned;
    boolean buyerSigned;

    LocalDateTime sellerSignedAt;
    LocalDateTime buyerSignedAt;

    ContractStatus status;

    String content;
}
