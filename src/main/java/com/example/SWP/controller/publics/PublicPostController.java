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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicPostController {

    PublicPostService publicPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts() {
        List<PostResponse> posts = publicPostService.getAllPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Tất cả bài đăng đã được lấy thành công")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/type/{productType}")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPostsByProductType(@PathVariable ProductType productType) {
        List<PostResponse> posts = publicPostService.getPostsByProductType(productType);
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Bài đăng theo loại sản phẩm đã được lấy thành công")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/priority")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPriorityPosts() {
        List<PostResponse> posts = publicPostService.getPriorityPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Bài đăng ưu tiên đã được lấy thành công")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/trusted")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getTrustedPosts() {
        List<PostResponse> posts = publicPostService.getTrustedPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Bài đăng uy tín đã được lấy thành công")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/post-detail")
    public ResponseEntity<ApiResponse<PostResponse>> getPostDetail(@RequestParam Long postId, Authentication authentication){
        PostResponse response = publicPostService.getPostById(authentication, postId);
        return ResponseEntity.ok(
                ApiResponse.<PostResponse>builder()
                        .success(true)
                        .message("Chi tiết bài đăng đã được lấy thành công")
                        .data(response)
                        .build()
        );
    }

}