package com.example.SWP.controller.user;

import com.example.SWP.dto.request.UpdateUserRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.UserProfileResponse;
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
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        UserProfileResponse profile = userService.getUserProfile(authentication);
        if (profile == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.<UserProfileResponse>builder()
                            .success(false)
                            .message("User not found")
                            .build());
        }
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Profile fetched successfully")
                .data(profile)
                .build());
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication authentication,
            @RequestBody UpdateUserRequest request
    ) {
        UserProfileResponse updatedProfile = userService.updateUserProfile(authentication, request);

        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(updatedProfile)
                .build());
    }



}
