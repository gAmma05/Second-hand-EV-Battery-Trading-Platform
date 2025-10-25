package com.example.SWP.dto.response;

import com.example.SWP.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String email;
    private String fullName;
    private String address;
    String streetAddress;
    Integer provinceId;
    Integer districtId;
    String wardCode;
    private String phone;
    private String avatar;
    private String storeName;
    private String storeDescription;
    private String socialMedia;
    private int remainingPosts;
    private Role role;
    private boolean status;
    private Long sellerPackageId;
    private LocalDateTime planExpiry;
}

