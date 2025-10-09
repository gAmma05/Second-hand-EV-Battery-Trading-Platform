package com.example.SWP.dto.request.buyer;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpgradeToSellerRequest {
    String shopName;
    String shopDescription;
    String socialMedia;
}
