package com.example.SWP.dto.response.seller;

import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.ProductType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    Long id;

    ProductType productType;
    String title;
    String description;

    UserResponse user;

    BigDecimal price;

    LocalDateTime postDate;
    LocalDateTime updateDate;
    LocalDateTime expiryDate;

    int viewCount;
    int likeCount;

    Set<DeliveryMethod> deliveryMethods;
    Set<PaymentType> paymentTypes;

    boolean trusted;

    Long priorityPackageId;
    LocalDateTime priorityExpire;

    PostStatus status;

    Long userId;

    List<String> images;

    Integer weight;

    boolean wantsTrustedLabel;

    String vehicleBrand;
    String model;
    Integer yearOfManufacture;
    String color;
    Integer mileage;

    String batteryType;
    Integer capacity;
    String voltage;
    String batteryBrand;
}
