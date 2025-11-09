package com.example.SWP.dto.response;

import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.ComplaintType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComplaintResponse {
    Long id;
    Long orderId;
    ComplaintType type;
    String name;
    String description;
    String evidenceUrls;
    String resolutionNotes;
    String createdAt;
    String updatedAt;
    ComplaintStatus status;
}
