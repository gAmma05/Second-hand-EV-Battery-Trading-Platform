package com.example.SWP.controller.admin;


import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.service.admin.AdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {
    AdminPostService adminPostService;

    @PutMapping("/{postId}/approve")
    public ResponseEntity<ApiResponse<Void>> approvePost(@PathVariable Long postId) {
        adminPostService.approvePost(postId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Post approved successfully")
                        .build()
        );
    }

    @PutMapping("/{postId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectPost(@PathVariable Long postId) {
        adminPostService.rejectPost(postId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Post rejected successfully")
                        .build()
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Post>>> getPendingPosts() {
        List<Post> pendingPosts = adminPostService.getPendingPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<Post>>builder()
                        .success(true)
                        .message("Pending posts fetched successfully")
                        .data(pendingPosts)
                        .build()
        );
    }
}

