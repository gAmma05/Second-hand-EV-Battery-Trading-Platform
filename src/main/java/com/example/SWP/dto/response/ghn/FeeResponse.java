package com.example.SWP.dto.response.ghn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeResponse {
    private Integer total;
    private Integer service_fee;
    private Integer insurance_fee;
}
