package com.example.SWP.controller.user;

import com.example.SWP.dto.request.user.ChangePasswordRequest;
import com.example.SWP.dto.request.user.UpdateUserRequest;


import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        UserResponse profile = userService.getUserProfile(authentication);
        if (profile == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("User not found")
                            .build());
        }
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile fetched successfully")
                .data(profile)
                .build());
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @RequestBody UpdateUserRequest request
    ) {
        UserResponse updatedProfile = userService.updateUserProfile(authentication, request);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(updatedProfile)
                .build());
    }

    @PatchMapping("/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            Authentication authentication,
            @RequestBody String avatarUrl
    ) {
        UserResponse updated = userService.updateAvatar(authentication, avatarUrl);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Avatar updated successfully")
                .data(updated)
                .build());
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(authentication, request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Password changed successfully")
                        .build()
        );
    }

}
