package com.example.SWP.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {
    String fullName;
    String phone;
    String storeName;
    String storeDescription;
    String socialMedia;

    String streetAddress;
    Integer provinceId;
    Integer districtId;
    String wardCode;

    String ghnToken;
    Integer ghnShopId;
}
