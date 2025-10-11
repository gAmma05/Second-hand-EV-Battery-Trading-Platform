package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.service.seller.PostService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/posts")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Post>> createPost(
            Authentication authentication,
            @RequestBody CreatePostRequest request
    ) {
        // Lấy sellerId tu authentication
        Long userId = (Long) authentication.getPrincipal();

        Post post = postService.createPost(userId, request);
        return ResponseEntity.ok(
                ApiResponse.<Post>builder()
                        .success(true)
                        .message("Post created successfully")
                        .data(post)
                        .build()
        );
    }
}
