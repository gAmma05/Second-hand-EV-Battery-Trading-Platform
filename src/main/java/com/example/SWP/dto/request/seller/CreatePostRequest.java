package com.example.SWP.dto.request.seller;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {
     String productType;
     String title;
     String description;
     double price;
     String address;
     boolean isPriority;
     Set<DeliveryMethod> deliveryMethods;
     Set<PaymentType> paymentTypes;
}
