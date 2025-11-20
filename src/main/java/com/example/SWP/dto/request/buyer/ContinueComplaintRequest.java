package com.example.SWP.dto.request.buyer;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContinueComplaintRequest {
    @NotNull(message = "Id của khiếu nại không được để trống")
    Long complaintId;

    @NotNull(message = "Thông tin khiếu nại lại không được để trống")
    String reDescription;
}
