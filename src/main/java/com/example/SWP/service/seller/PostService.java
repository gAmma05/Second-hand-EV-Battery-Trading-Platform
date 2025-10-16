package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.request.seller.UpdatePostRequest;
import com.example.SWP.dto.response.CreatePostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.entity.User;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.SellerPackageType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.PriorityPackageRepository;
import com.example.SWP.repository.SellerPackageRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    PostRepository postRepository;
    UserRepository userRepository;
    SellerPackageRepository sellerPackageRepository;
    PaymentService paymentService;
    ValidateService validateService;
    PriorityPackageRepository priorityPackageRepository;
    WalletService walletService;
    WalletRepository walletRepository;

    @NonFinal
    @Value("${post.expire.days}")
    int expireDays;

    @NonFinal
    @Value("${post.update.limitDays}")
    int limitDays;

    public CreatePostResponse createPost(Authentication authentication, CreatePostRequest request) {

        User user = validateService.validateCurrentUser(authentication);

        if (user.getRemainingPosts() <= 0) {
            throw new BusinessException(
                    "You have no remaining post slots. Please upgrade your plan or wait for reset.",
                    400
            );
        }

        SellerPackage sellerPackage = null;
        if (user.getSellerPackageId() != null) {
            sellerPackage = sellerPackageRepository.findById(user.getSellerPackageId()).orElse(null);
        }

        Post post = Post.builder()
                .user(user)
                .productType(request.getProductType())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .address(request.getAddress())
                .priorityPackageId(request.getPriorityPackageId())
                .deliveryMethods(request.getDeliveryMethods())
                .paymentTypes(request.getPaymentTypes())
                .postDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays(expireDays))
                .viewCount(0)
                .likeCount(0)
                .build();

        if (sellerPackage == null || sellerPackage.getType() == SellerPackageType.BASIC) {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
        } else if (sellerPackage.getType() == SellerPackageType.PREMIUM) {
            post.setStatus(PostStatus.PENDING);
        }

        user.setRemainingPosts(user.getRemainingPosts() - 1);
        userRepository.save(user);

        String paymentUrl = null;
        if (request.getPriorityPackageId() != null) {

            // Thanh toán bằng Wallet
            if (request.getIsUseWallet()) {
                walletService.payPriorityPackage(user, request.getPriorityPackageId());
                post.setPriorityPackageId(request.getPriorityPackageId());
                post.setStatus(PostStatus.PENDING);

            }
            // Thanh toán VNPay
            else {
                paymentUrl = paymentService.priorityPackagePayment(user.getEmail(), post.getId(), request.getPriorityPackageId());
            }
        }

        post = postRepository.save(post);

        return CreatePostResponse.builder()
                .post(post)
                .paymentUrl(paymentUrl) // sẽ null nếu dùng wallet
                .build();
    }


    public Post updatePost(Authentication authentication, Long postId, UpdatePostRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to update this post", 403);
        }

        if (post.getPostDate().plusDays(limitDays).isBefore(LocalDateTime.now())) {
            throw new BusinessException("You cannot update this post after " + limitDays + " days from posting", 400);
        }

        post.setProductType(request.getProductType());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setAddress(request.getAddress());
        post.setDeliveryMethods(request.getDeliveryMethods());
        post.setPaymentTypes(request.getPaymentTypes());
        post.setUpdateDate(LocalDateTime.now());

        SellerPackage sellerPackage = null;
        if (user.getSellerPackageId() != null) {
            sellerPackage = sellerPackageRepository.findById(user.getSellerPackageId()).orElse(null);
        }

        if (sellerPackage == null) {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
        } else {
            switch (sellerPackage.getType()) {
                case BASIC -> {
                    post.setStatus(PostStatus.POSTED);
                    post.setTrusted(false);
                }
                case PREMIUM -> {
                    post.setStatus(PostStatus.PENDING);
                }
            }
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

    public List<Post> getMyPosts(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        return postRepository.findByUserAndStatusNot(user, PostStatus.DELETED);
    }
}
