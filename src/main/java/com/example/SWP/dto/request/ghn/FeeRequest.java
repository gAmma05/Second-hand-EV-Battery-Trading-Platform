package com.example.SWP.dto.request.ghn;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FeeRequest {
    Integer fromDistrictId;
    Integer toDistrictId;
    String toWardCode;
    Integer serviceTypeId;

    Integer weight;

    String ghnToken;
    Integer ghnShopId;
}
