package com.example.SWP.dto.response;

import com.example.SWP.enums.AuthProvider;
import com.example.SWP.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String email;
    private String fullName;
    private Role role;
    private AuthProvider provider;
    private String avatar;
    private String phone;
    private String address;
    private boolean status;
}
