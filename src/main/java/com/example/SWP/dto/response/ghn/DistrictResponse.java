package com.example.SWP.dto.response.ghn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistrictResponse {
    private  int districtId;
    private String districtName;
}
