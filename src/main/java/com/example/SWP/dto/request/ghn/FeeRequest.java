package com.example.SWP.dto.request.ghn;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FeeRequest {
    Long postId;
    Integer serviceTypeId;
}
