package com.example.SWP.dto.response.seller;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.ProductType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractTemplateResponse {
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

    BigDecimal depositPercentage;
}
