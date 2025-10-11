package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.SellerPlan;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    PostRepository postRepository;
    UserRepository userRepository;

    @NonFinal
    @Value("${post.expire.days}")
    int expireDays;

    @NonFinal
    @Value("${post.update.limitDays}")
    int limitDays;

    public Post createPost(Authentication authentication, CreatePostRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        if (user.getRemainingPosts() <= 0) {
            throw new BusinessException(
                    "You have no remaining post slots. Please upgrade your plan or wait for reset.",
                    400
            );
        }

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
                .viewCount(0)
                .likeCount(0)
                .build();

        if (user.getSellerPlan() == SellerPlan.BASIC) {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
        } else if (user.getSellerPlan() == SellerPlan.PREMIUM) {
            post.setStatus(PostStatus.PENDING);
        }

        user.setRemainingPosts(user.getRemainingPosts() - 1);

        return postRepository.save(post);
    }

    public Post updatePost(Authentication authentication, Long postId, CreatePostRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to update this post", 403);
        }

        if (post.getExpiryDate().isBefore(LocalDateTime.now().minusDays(limitDays))) {
            throw new BusinessException("You cannot update an expired post after " + limitDays + " days", 400);
        }

        post.setProductType(request.getProductType());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setAddress(request.getAddress());
        post.setPriority(request.isPriority());
        post.setDeliveryMethods(request.getDeliveryMethods());
        post.setPaymentTypes(request.getPaymentTypes());
        post.setUpdateDate(LocalDateTime.now());

        if (user.getSellerPlan() == SellerPlan.BASIC) {
            post.setStatus(PostStatus.POSTED);
        } else if (user.getSellerPlan() == SellerPlan.PREMIUM) {
            post.setStatus(PostStatus.PENDING);
        }

        return postRepository.save(post);
    }

    public void deletePost(Authentication authentication, Long postId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to delete this post", 403);
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    public List<Post> getPostsBySeller(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        return postRepository.findByUserAndStatusNot(user, PostStatus.DELETED);
    }
}
