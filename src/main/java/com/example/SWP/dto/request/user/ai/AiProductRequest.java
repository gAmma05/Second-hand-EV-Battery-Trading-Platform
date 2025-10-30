package com.example.SWP.dto.request.user.ai;

import com.example.SWP.enums.ProductType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class AiProductRequest {
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

    String description;
}
