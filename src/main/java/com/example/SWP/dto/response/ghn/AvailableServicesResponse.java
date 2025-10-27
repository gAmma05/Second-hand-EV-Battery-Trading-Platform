package com.example.SWP.dto.response.ghn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableServicesResponse {
    private Integer service_id;
    private String short_name;
    private Integer service_type_id;
}
