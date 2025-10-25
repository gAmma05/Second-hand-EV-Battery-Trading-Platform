package com.example.SWP.dto.request.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpgradeToSellerRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name must not exceed 100 characters")
    String shopName;

    @NotBlank(message = "Shop description is required")
    @Size(max = 500, message = "Shop description must not exceed 500 characters")
    String shopDescription;

    @NotBlank(message = "Social media is required")
    @Size(max = 200, message = "Social media must not exceed 200 characters")
    String socialMedia;

    @NotBlank(message = "GHN Token is required")
    String ghnToken;

    @NotNull(message = "GHN Shop ID is required")
    Integer ghnShopId;
}
