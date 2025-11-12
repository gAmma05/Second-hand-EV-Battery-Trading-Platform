package com.example.SWP.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppConfigRequest {
    @NotBlank(message = "Key cấu hình không được để trống")
    @Size(max = 100, message = "Key cấu hình quá dài (tối đa 100 ký tự)")
    String configKey;

    @NotBlank(message = "Giá trị cấu hình không được để trống")
    @Size(max = 500, message = "Giá trị cấu hình quá dài (tối đa 500 ký tự)")
    String configValue;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    String description;
}
