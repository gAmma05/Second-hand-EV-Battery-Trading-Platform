package com.example.SWP.dto.request.seller;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePostRequest {
    @NotBlank(message = "Product type is required")
    @Size(max = 100, message = "Product type must not exceed 100 characters")
    String productType;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description;

    @Positive(message = "Price must be greater than 0")
    BigDecimal price;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address;

    @NotEmpty(message = "At least one delivery method is required")
    Set<DeliveryMethod> deliveryMethods;

    @NotEmpty(message = "At least one payment type is required")
    Set<PaymentType> paymentTypes;
}
