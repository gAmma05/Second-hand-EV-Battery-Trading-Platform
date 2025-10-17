package com.example.SWP.dto.response.seller;

import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.PostStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    Long id;

    String productType;
    String title;
    String description;

    BigDecimal price;
    double suggestPrice;

    LocalDateTime postDate;
    LocalDateTime updateDate;
    LocalDateTime expiryDate;

    int viewCount;
    int likeCount;

    Set<DeliveryMethod> deliveryMethods;
    Set<PaymentType> paymentTypes;

    String address;
    boolean isTrusted;

    Long priorityPackageId;
    LocalDateTime priorityExpire;

    PostStatus status;

    UserResponse user;
}
