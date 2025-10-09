package com.example.SWP.dto.request;

import com.example.SWP.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCreateUserRequest {
    String email;
    String password;
    String fullName;
    Role role;
}
