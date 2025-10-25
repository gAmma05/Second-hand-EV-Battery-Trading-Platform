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
public class GhnShippingFeeRequest {
    Integer fromDistrictId;
    Integer toDistrictId;
    String toWardCode;
    Integer serviceTypeId;

    Integer weight;

    String ghnToken;
    String ghnShopId;
}
