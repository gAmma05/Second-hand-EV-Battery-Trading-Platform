package com.example.SWP.dto.request.admin;

import com.example.SWP.enums.ResolutionType;
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

    @NotNull(message = "Resolution type is required")
    ResolutionType resolutionType;

}
