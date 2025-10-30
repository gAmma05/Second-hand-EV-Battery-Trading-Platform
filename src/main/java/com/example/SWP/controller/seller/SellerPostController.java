package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.request.seller.UpdatePostRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.service.seller.SellerPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/posts")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerPostController {

    SellerPostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            Authentication authentication,
            @Valid @RequestBody CreatePostRequest request
    ) {
        PostResponse post = postService.createPost(authentication, request);

        return ResponseEntity.ok(
                ApiResponse.<PostResponse>builder()
                        .success(true)
                        .message("Post created successfully")
                        .data(post)
                        .build()
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request
    ) {
        PostResponse updatedPost = postService.updatePost(authentication, postId, request);
        return ResponseEntity.ok(
                ApiResponse.<PostResponse>builder()
                        .success(true)
                        .message("Post updated successfully")
                        .data(updatedPost)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts(Authentication authentication) {
        List<PostResponse> posts = postService.getMyPosts(authentication);
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("List of your posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        PostResponse post = postService.getPostById(authentication, postId);
        return ResponseEntity.ok(
                ApiResponse.<PostResponse>builder()
                        .success(true)
                        .message("Post retrieved successfully")
                        .data(post)
                        .build()
        );
    }


    @GetMapping("/posts/status/{status}")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPostsByStatus(
            Authentication authentication,
            @PathVariable PostStatus status) {
        List<PostResponse> posts = postService.getMyPostsByStatus(authentication, status);
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        postService.deletePost(authentication, postId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Post deleted successfully")
                        .build()
        );
    }
}
