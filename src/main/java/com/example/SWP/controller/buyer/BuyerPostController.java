package com.example.SWP.controller.buyer;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.buyer.PostFavoriteResponse;
import com.example.SWP.dto.response.seller.ComparePostsResponse;
import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.service.buyer.BuyerPostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/likes")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyLikedPosts(Authentication authentication) {
        List<PostResponse> likedPosts = buyerPostService.getMyLikedPosts(authentication);

        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .success(true)
                        .message("Liked posts retrieved successfully")
                        .data(likedPosts)
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
    public ResponseEntity<ApiResponse<ComparePostsResponse>> comparePosts(
            Authentication authentication,
            @RequestParam Long postId1,
            @RequestParam Long postId2) {

        ComparePostsResponse response = buyerPostService.comparePosts(authentication, postId1, postId2);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<ComparePostsResponse>builder()
                        .success(true)
                        .message("Comparison result")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/{postId}/favorite")
    public ResponseEntity<ApiResponse<Void>> addToFavorites(Authentication authentication,
                                                            @PathVariable Long postId) {
        buyerPostService.addToFavorites(authentication, postId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Added to favorites successfully")
                        .build()
        );
    }

    @DeleteMapping("/{postId}/favorite")
    public ResponseEntity<ApiResponse<Void>> removeFromFavorites(Authentication authentication,
                                                                 @PathVariable Long postId) {
        buyerPostService.removeFromFavorites(authentication, postId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Removed from favorites successfully")
                        .build()
        );
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<?>> getMyFavorites(Authentication authentication) {
        List<PostFavoriteResponse> favorites = buyerPostService.getMyFavorites(authentication);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Favorite posts retrieved successfully")
                        .data(favorites)
                        .build()
        );
    }
}
