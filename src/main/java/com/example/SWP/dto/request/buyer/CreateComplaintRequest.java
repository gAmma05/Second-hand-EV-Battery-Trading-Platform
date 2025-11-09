package com.example.SWP.dto.request.buyer;

import com.example.SWP.entity.ComplaintImage;
import com.example.SWP.enums.ComplaintType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplaintRequest {
    @NotNull(message = "ID của order bị trống")
    Long orderId;

    @NotNull(message = "Phần loại khiếu nại không được để trống")
    ComplaintType complaintType;

    @NotNull(message = "Phần mô tả không được để trống")
    String description;

    @NotNull(message = "Bằng chứng không được để trống")
    @NotEmpty(message = "Bằng chứng không được để trống")
    List<String> complaintImages;
}
