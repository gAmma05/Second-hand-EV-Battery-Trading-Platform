package com.example.SWP.dto.request.admin;

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
public class HandleComplaintRequest {

    @NotNull(message = "Complaint ID is required")
    Long complaintId;

    @NotNull(message = "Resolution is required")
    String resolution;
}
