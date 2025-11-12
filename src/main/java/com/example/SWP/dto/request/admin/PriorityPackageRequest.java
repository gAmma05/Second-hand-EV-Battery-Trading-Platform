package com.example.SWP.dto.request.admin;

import com.example.SWP.enums.PriorityPackageType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriorityPackageRequest {

    @NotNull(message = "Mô tả không được để trống")
    @Size(min = 10, max = 1000, message = "Mô tả phải có từ 10 đến 1000 ký tự")
    String description;

    @Min(value = 1, message = "Thời gian hiệu lực phải ít nhất là 1 ngày")
    int durationDays;

    @NotNull(message = "Giá gói không được để trống")
    @DecimalMin(value = "0.01", message = "Giá gói phải lớn hơn 0")
    BigDecimal price;

}
