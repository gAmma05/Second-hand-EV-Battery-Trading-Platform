package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.SellerPlan;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    PostRepository postRepository;
    UserRepository userRepository;


    @NonFinal
    @Value("${post.expire.days}")
    private int expireDays;

    public Post createPost(Long userId, CreatePostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRemainingPosts() <= 0) {
            throw new RuntimeException("You have no remaining post slots. Please upgrade your plan or wait for reset.");
        }

        //Tao bai dang
        Post post = Post.builder()
                .user(user)
                .productType(request.getProductType())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .address(request.getAddress())
                .isPriority(request.isPriority())
                .deliveryMethods(request.getDeliveryMethods())
                .paymentTypes(request.getPaymentTypes())
                .postDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays(expireDays))
                .status(PostStatus.PENDING)
                .viewCount(0)
                .linkCount(0)
                .build();

        // Kiem tra goi cua seller
        if (user.getSellerPlan() == SellerPlan.BASIC) {
            post.setStatus(PostStatus.POSTED); //Tu dong dang bai
            post.setTrusted(false);
        } else if (user.getSellerPlan() == SellerPlan.PREMIUM) {
            post.setStatus(PostStatus.PENDING);  // Cho admin duyet
            post.setTrusted(false);
        }

        //Giam luot dang
        user.setRemainingPosts(user.getRemainingPosts() - 1);

        return postRepository.save(post);
    }
}
