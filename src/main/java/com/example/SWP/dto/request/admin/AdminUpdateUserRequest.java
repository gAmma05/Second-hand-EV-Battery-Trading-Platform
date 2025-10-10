package com.example.SWP.dto.request.admin;

import com.example.SWP.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateUserRequest {
    String email;
    String fullName;
    String phone;
    String address;
    String avatar;
    Role role;
    Boolean status;
}
