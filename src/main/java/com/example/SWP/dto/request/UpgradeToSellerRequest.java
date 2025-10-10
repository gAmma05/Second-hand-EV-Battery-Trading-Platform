package com.example.SWP.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpgradeToSellerRequest {
    private String shopName;
    private String shopDescription;
    private String socialMedia;
    private int remainingPostCredits;
    private int cooldownDuration;
}
