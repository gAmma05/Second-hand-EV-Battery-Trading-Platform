package com.example.SWP.controller.buyer;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.service.buyer.BuyerPostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buyer/posts")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BuyerPostController {

    BuyerPostService buyerPostService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(Authentication authentication,
                                                      @PathVariable Long postId) {
        buyerPostService.likePost(authentication, postId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Liked post successfully")
                        .build()
        );
    }

    @DeleteMapping("/{postId}/unlike")
    public ResponseEntity<ApiResponse<Void>> unlikePost(Authentication authentication,
                                                        @PathVariable Long postId) {
        buyerPostService.unlikePost(authentication, postId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Unliked post successfully")
                        .build()
        );
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<String>> comparePosts(
            Authentication authentication,
            @RequestParam Long postId1,
            @RequestParam Long postId2) {

        String summary = buyerPostService.comparePosts(authentication, postId1, postId2);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Comparison result")
                        .data(summary)
                        .build()
        );
    }
}
