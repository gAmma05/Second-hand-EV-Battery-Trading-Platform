package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.buyer.PostFavoriteResponse;
import com.example.SWP.dto.response.seller.ComparePostsResponse;
import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.PostFavorite;
import com.example.SWP.entity.PostLike;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.PostFavoriteMapper;
import com.example.SWP.mapper.PostMapper;
import com.example.SWP.repository.PostFavoriteRepository;
import com.example.SWP.repository.PostLikeRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.service.ai.AiService;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerPostService {

    ValidateService validateService;
    PostRepository postRepository;
    PostLikeRepository postLikeRepository;
    AiService  aiService;
    PostFavoriteRepository postFavoriteRepository;
    PostFavoriteMapper postFavoriteMapper;
    PostMapper postMapper;

    public void likePost(Authentication authentication, Long postId) {
        User user = validateService.validateCurrentUser(authentication);

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("Không tìm thấy bài đăng", 404)
        );

        if(post.getStatus() != PostStatus.POSTED) {
            throw new BusinessException("Bài đăng chưa được đăng lên, không thể like bài đăng này", 404);
        }

        if (postLikeRepository.existsByBuyerAndPost(user, post)) {
            throw new BusinessException("Bạn đã thích bài viết này trước đó", 400);
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .buyer(user)
                .build();
        postLikeRepository.save(postLike);

        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }


    public void unlikePost(Authentication authentication, Long postId) {
        User user = validateService.validateCurrentUser(authentication);

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("Không tìm thấy bài đăng", 404)
        );

        if(post.getStatus() != PostStatus.POSTED) {
            throw new BusinessException("Bài đăng chưa được đăng lên, không thể bỏ thích bài đăng này", 404);
        }

        PostLike postLike = postLikeRepository.findByBuyerAndPost(user, post)
                .orElseThrow(() -> new BusinessException("Bạn chưa thích bài đăng này trước đó", 400));

        postLikeRepository.delete(postLike);

        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
        }
    }

    public ComparePostsResponse comparePosts(Authentication authentication, Long postId1, Long postId2) {
        User user = validateService.validateCurrentUser(authentication);

        Post post1 = postRepository.findById(postId1).orElseThrow(
                () -> new BusinessException("Không tìm thấy bài đăng 1", 404)
        );

        Post post2 = postRepository.findById(postId2).orElseThrow(
                () -> new BusinessException("Không tìm thấy bài đăng 2", 404)
        );

        if(post1.getId().equals(post2.getId())) {
            throw new BusinessException("Không thể so sánh 2 bài đăng giống nhau", 400);
        }

        if(post1.getProductType() != post2.getProductType()) {
            throw new BusinessException("Cả hai bài đăng phải cùng loại hình/sản phẩm thì mới có thể so sánh", 400);
        }

        if (post1.getStatus() != PostStatus.POSTED || post2.getStatus() != PostStatus.POSTED) {
            throw new BusinessException("Cả hai bài đăng đều phải được đăng lên hệ thống thì mới được so sánh", 400);
        }

        AiProductRequest req1 = AiProductRequest.builder()
                .productType(post1.getProductType())
                .vehicleBrand(post1.getVehicleBrand())
                .model(post1.getModel())
                .yearOfManufacture(post1.getYearOfManufacture())
                .color(post1.getColor())
                .mileage(post1.getMileage())
                .batteryBrand(post1.getBatteryBrand())
                .batteryType(post1.getBatteryType())
                .capacity(post1.getCapacity())
                .voltage(post1.getVoltage())
                .description(post1.getDescription())
                .build();

        AiProductRequest req2 = AiProductRequest.builder()
                .productType(post2.getProductType())
                .vehicleBrand(post2.getVehicleBrand())
                .model(post2.getModel())
                .yearOfManufacture(post2.getYearOfManufacture())
                .color(post2.getColor())
                .mileage(post2.getMileage())
                .batteryBrand(post2.getBatteryBrand())
                .batteryType(post2.getBatteryType())
                .capacity(post2.getCapacity())
                .voltage(post2.getVoltage())
                .description(post2.getDescription())
                .build();

        String comparisonResult = aiService.compareProduct(req1, req2);

        PostResponse postResponse1 = postMapper.toPostResponse(post1);
        PostResponse postResponse2 = postMapper.toPostResponse(post2);

        return ComparePostsResponse.builder()
                .post1(postResponse1)
                .post2(postResponse2)
                .comparisonResult(comparisonResult)
                .build();
    }


    public void addToFavorites(Authentication authentication, Long postId) {
        User user = validateService.validateCurrentUser(authentication);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bài đăng", 404));

        if (postFavoriteRepository.existsByBuyerAndPost(user, post)) {
            throw new BusinessException("Đã có sẵn trong danh sách yêu thích của bạn", 400);
        }

        PostFavorite favorite = PostFavorite.builder()
                .buyer(user)
                .post(post)
                .build();

        postFavoriteRepository.save(favorite);
    }

    public void removeFromFavorites(Authentication authentication, Long postId) {
        User user = validateService.validateCurrentUser(authentication);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bài đăng", 404));

        PostFavorite favorite = postFavoriteRepository.findByBuyerAndPost(user, post)
                .orElseThrow(() -> new BusinessException("Bài đăng này chưa có sẵn trong danh sách yêu thích của bạn", 400));

        postFavoriteRepository.delete(favorite);
    }

    public List<PostFavoriteResponse> getMyFavorites(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);
        List<PostFavorite> results = postFavoriteRepository.findAllByBuyer(user);
        return postFavoriteMapper.toPostFavoriteResponseList(results);
    }

    public List<PostResponse> getMyLikedPosts(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<PostLike> likedPosts = postLikeRepository.findAllByBuyer(user);

        return likedPosts.stream()
                .map(postLike -> postMapper.toPostResponse(postLike.getPost()))
                .toList();
    }
}
