package com.example.SWP.dto.request.ghn;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceRequest {
    String ghnToken;
    Integer ghnShopId;
    Integer fromDistrictId;
    Integer toDistrictId;
}
