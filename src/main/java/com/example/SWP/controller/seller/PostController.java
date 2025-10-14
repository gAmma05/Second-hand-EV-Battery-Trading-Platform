package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.request.seller.UpdatePostRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.CreatePostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.service.seller.PostService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/posts")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            Authentication authentication,
            @RequestBody CreatePostRequest request
    ) {
        CreatePostResponse response = postService.createPost(authentication, request);

        return ResponseEntity.ok(
                ApiResponse.<CreatePostResponse>builder()
                        .success(true)
                        .message("Post created successfully")
                        .data(response)
                        .build()
        );
    }


    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Post>> updatePost(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request
    ) {
        Post updatedPost = postService.updatePost(authentication, postId, request);
        return ResponseEntity.ok(
                ApiResponse.<Post>builder()
                        .success(true)
                        .message("Post updated successfully")
                        .data(updatedPost)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Post>>> getMyPosts(Authentication authentication) {
        List<Post> posts = postService.getMyPosts(authentication);
        return ResponseEntity.ok(
                ApiResponse.<List<Post>>builder()
                        .success(true)
                        .message("List of your posts retrieved successfully")
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
