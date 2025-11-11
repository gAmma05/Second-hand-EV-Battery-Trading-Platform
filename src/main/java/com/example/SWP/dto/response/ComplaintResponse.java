package com.example.SWP.dto.response;

import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.ComplaintType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComplaintResponse {
    Long id;
    Long orderId;
    ComplaintType type;
    String sellerName;
    String buyerName;
    String description;
    List<String> imageUrls;
    String resolutionNotes;
    String createdAt;
    String updatedAt;
    ComplaintStatus status;
}
