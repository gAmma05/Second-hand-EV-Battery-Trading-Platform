package com.example.SWP.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyContractSignatureRequest {
    @NotNull(message = "ID hợp đồng là bắt buộc")
    Long contractId;

    @NotBlank(message = "OTP là bắt buộc")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP phải là mã số gồm 6 chữ số"
    )
    String otp;
}
