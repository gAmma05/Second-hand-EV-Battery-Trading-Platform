package com.example.SWP.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "OTP must not be blank")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    String otp;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String newPassword;

    @NotBlank(message = "Confirm password must not be blank")
    String confirmPassword;
}
