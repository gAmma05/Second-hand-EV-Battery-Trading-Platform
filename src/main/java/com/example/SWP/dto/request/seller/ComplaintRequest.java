package com.example.SWP.dto.request.seller;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ComplaintRequest {

    @NotNull(message = "ID bị trống")
    Long complaintId;

    String resolution;

    @NotNull(message = "Bạn chỉ có thể chấp nhận hoặc từ chối xử lí đơn khiếu nại này")
    boolean isAccepted;

    @NotNull(message = "Bạn phải lựa chọn hoàn tiền hoặc gửi khiếu nại lên admin xử lí")
    boolean requestToAdmin;
}