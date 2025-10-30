package com.example.SWP.dto.request.seller;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.ProductType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {

    @NotNull(message = "Product type is required")
    ProductType productType;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description;

    @Positive(message = "Price must be greater than 0")
    BigDecimal price;

    Long priorityPackageId;

    @NotEmpty(message = "At least one delivery method is required")
    Set<DeliveryMethod> deliveryMethods;

    @NotEmpty(message = "At least one payment type is required")
    Set<PaymentType> paymentTypes;

    @NotEmpty(message = "At least one image is required")
    List<@NotBlank(message = "Image URL cannot be blank") String> images;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be greater than 0")
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
