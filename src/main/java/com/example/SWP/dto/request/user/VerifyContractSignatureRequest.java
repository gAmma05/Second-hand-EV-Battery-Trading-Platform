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
    @NotNull(message = "Contract ID is required")
    Long contractId;

    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must be a 6-digit numeric code"
    )
    String otp;
}
