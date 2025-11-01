package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.PostLike;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PostLikeRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.service.ai.AiService;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerPostService {

    ValidateService validateService;
    PostRepository postRepository;
    PostLikeRepository postLikeRepository;
    AiService  aiService;

    public void likePost(Authentication authentication, Long postId) {
        User user = validateService.validateCurrentUser(authentication);

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("Post not found", 404)
        );

        if(post.getStatus() != PostStatus.POSTED) {
            throw new BusinessException("Post not posted", 404);
        }

        if (postLikeRepository.existsByBuyerAndPost(user, post)) {
            throw new BusinessException("You already liked this post!", 400);
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
                () -> new BusinessException("Post not found", 404)
        );

        if(post.getStatus() != PostStatus.POSTED) {
            throw new BusinessException("Post not posted", 404);
        }

        PostLike postLike = postLikeRepository.findByBuyerAndPost(user, post)
                .orElseThrow(() -> new BusinessException("You haven't liked this post yet", 400));

        postLikeRepository.delete(postLike);

        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
        }
    }

    public String comparePosts(Authentication authentication, Long postId1, Long postId2) {
        User user = validateService.validateCurrentUser(authentication);

        Post post1 = postRepository.findById(postId1).orElseThrow(
                () -> new BusinessException("Post not found", 404)
        );

        Post post2 = postRepository.findById(postId2).orElseThrow(
                () -> new BusinessException("Post not found", 404)
        );

        if(post1.getId().equals(post2.getId())) {
            throw new BusinessException("Can not compare the same post", 400);
        }

        if(post1.getProductType() != post2.getProductType()) {
            throw new BusinessException("Posts must be of the same product type to compare", 400);
        }

        if (post1.getStatus() != PostStatus.POSTED || post2.getStatus() != PostStatus.POSTED) {
            throw new BusinessException("Both posts must be in POSTED status to compare", 400);
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

        return aiService.compareProduct(req1, req2);
    }


}
