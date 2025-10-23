package com.example.SWP.dto.response.ghn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponse {
    private int service_id;
    private int service_type_id;
    private String short_name;
}
