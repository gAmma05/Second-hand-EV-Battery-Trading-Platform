package com.example.SWP.controller.admin;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.UserResponse;
import com.example.SWP.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PutMapping("/{id}/block")
    public ResponseEntity<ApiResponse<UserResponse>> blockUser(@PathVariable Long id) {
        UserResponse user = adminUserService.blockUser(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User blocked successfully")
                .data(user)
                .build());
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<UserResponse>> unblockUser(@PathVariable Long id) {
        UserResponse user = adminUserService.unblockUser(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User unblocked successfully")
                .data(user)
                .build());
    }
}

