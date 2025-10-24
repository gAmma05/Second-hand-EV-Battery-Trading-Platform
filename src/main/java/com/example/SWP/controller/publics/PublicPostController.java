package com.example.SWP.controller.publics;

import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.enums.ProductType;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.service.publics.PublicPostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicPostController {

    PublicPostService publicPostService;

    // Lấy tất cả post
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts() {
        List<PostResponse> posts = publicPostService.getAllPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("All posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    // Lấy post theo ProductType
    @GetMapping("/type/{productType}")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPostsByProductType(@PathVariable ProductType productType) {
        List<PostResponse> posts = publicPostService.getPostsByProductType(productType);
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Posts by product type retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    // Lấy tất cả post ưu tiên còn hiệu lực
    @GetMapping("/priority")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPriorityPosts() {
        List<PostResponse> posts = publicPostService.getPriorityPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Priority posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    // Lấy tất cả post trusted
    @GetMapping("/trusted")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getTrustedPosts() {
        List<PostResponse> posts = publicPostService.getTrustedPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Trusted posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/post-detail")
    public ResponseEntity<ApiResponse<PostResponse>> getPostDetail(@RequestParam Long postId){
        PostResponse response = publicPostService.getPostById(postId);
        return ResponseEntity.ok(
                ApiResponse.<PostResponse>builder()
                        .success(true)
                        .message("Post detail retrieved successfully")
                        .data(response)
                        .build()
        );
    }

}
