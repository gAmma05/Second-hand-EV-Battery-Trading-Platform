package com.example.SWP.dto.request.buyer;

import com.example.SWP.enums.ComplaintType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplaintRequest {
    Long orderId;
    ComplaintType complaintType;

    @NotNull(message = "Description is required")
    String description;

    String evidenceUrls;
}
