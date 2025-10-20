package com.example.SWP.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "Current password must not be blank")
    String currentPassword;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 6, message = "New password must be at least 6 characters")
    String newPassword;

    @NotBlank(message = "Confirm password must not be blank")
    String confirmPassword;
}
