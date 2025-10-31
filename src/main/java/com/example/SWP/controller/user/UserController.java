package com.example.SWP.controller.user;

import com.example.SWP.dto.request.user.ChangePasswordRequest;
import com.example.SWP.dto.request.user.UpdateAvatarRequest;
import com.example.SWP.dto.request.user.UpdateUserRequest;


import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.service.user.UserService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody UpdateAvatarRequest request
    ) {
        UserResponse updated = userService.updateAvatar(authentication, request.getAvatar());
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Avatar updated successfully")
                .data(updated)
                .build());
    }


    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("New password and confirm password do not match", 400);
        }

        userService.changePassword(authentication, request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Password changed successfully")
                        .build()
        );
    }

}
