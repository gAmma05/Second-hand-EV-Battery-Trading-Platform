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
public class CreateUserRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password;

    @NotBlank(message = "Please confirm your password")
    String confirmPassword;

    @NotBlank(message = "Full name must not be blank")
    String fullName;
}
