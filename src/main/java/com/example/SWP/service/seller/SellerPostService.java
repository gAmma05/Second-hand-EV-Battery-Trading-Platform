package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.request.seller.UpdatePostRequest;
import com.example.SWP.entity.*;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.ProductType;
import com.example.SWP.enums.SellerPackageType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.PriorityPackagePaymentRepository;
import com.example.SWP.repository.SellerPackageRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerPostService {

    PostRepository postRepository;
    UserRepository userRepository;
    SellerPackageRepository sellerPackageRepository;
    ValidateService validateService;
    SellerPaymentService sellerPaymentService;
    PriorityPackagePaymentRepository priorityPackagePaymentRepository;

    @NonFinal
    @Value("${post.expire.days}")
    int expireDays;

    @NonFinal
    @Value("${post.update.limitDays}")
    int limitDays;

    public Post createPost(Authentication authentication, CreatePostRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        if (user.getRemainingPosts() <= 0) {
            throw new BusinessException(
                    "You have no remaining post slots. Please upgrade your plan or wait for reset.",
                    400
            );
        }

        // Validate theo productType
        if (request.getProductType() == ProductType.VEHICLE) {
            if (request.getVehicleBrand() == null || request.getModel() == null || request.getYearOfManufacture() == null) {
                throw new BusinessException("Brand, model, and yearOfManufacture are required for VEHICLE", 400);
            }
        } else if (request.getProductType() == ProductType.BATTERY) {
            if (request.getBatteryType() == null || request.getCapacity() == null || request.getVoltage() == null) {
                throw new BusinessException("BatteryType, capacity, and voltage are required for BATTERY", 400);
            }
        }

        SellerPackage sellerPackage = null;
        if (user.getSellerPackageId() != null) {
            sellerPackage = sellerPackageRepository.findById(user.getSellerPackageId()).orElse(null);
        }

        // Build post
        Post.PostBuilder postBuilder = Post.builder()
                .user(user)
                .productType(request.getProductType())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .deliveryMethods(request.getDeliveryMethods())
                .paymentTypes(request.getPaymentTypes())
                .postDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays(expireDays))
                .viewCount(0)
                .likeCount(0)
                .weight(request.getWeight());

        // Gán thông số kỹ thuật theo type
        if (request.getProductType() == ProductType.VEHICLE) {
            postBuilder
                    .vehicleBrand(request.getVehicleBrand())
                    .model(request.getModel())
                    .yearOfManufacture(request.getYearOfManufacture())
                    .color(request.getColor())
                    .mileage(request.getMileage());
        } else if (request.getProductType() == ProductType.BATTERY) {
            postBuilder
                    .batteryType(request.getBatteryType())
                    .capacity(request.getCapacity())
                    .voltage(request.getVoltage())
                    .batteryBrand(request.getBatteryBrand());
        }

        Post post = postBuilder.build();

        // Set status và trusted theo gói
        if (sellerPackage == null || sellerPackage.getType() == SellerPackageType.BASIC) {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
        } else if (sellerPackage.getType() == SellerPackageType.PREMIUM) {
            post.setStatus(PostStatus.PENDING);
        }

        List<PostImage> postImages = new ArrayList<>();
        for (String url : request.getImages()) {
            PostImage image = PostImage.builder()
                    .post(post)
                    .imageUrl(url)
                    .build();
            postImages.add(image);
        }
        post.setImages(postImages);

        // Xử lý priority package nếu có
        if (request.getPriorityPackageId() != null) {
            PriorityPackagePayment payment = sellerPaymentService.priorityPackagePayment(user, request.getPriorityPackageId());
            post.setPriorityPackageId(request.getPriorityPackageId());
            LocalDateTime expireDate = LocalDateTime.now().plusDays(payment.getPriorityPackage().getDurationDays());
            post.setPriorityExpire(expireDate);
            post = postRepository.save(post); // save trước khi gán payment
            payment.setPost(post);
            priorityPackagePaymentRepository.save(payment);
        }

        // Lưu post (cùng lúc lưu ảnh nhờ cascade)
        post = postRepository.save(post);

        // Cập nhật số lượng post còn lại của user
        user.setRemainingPosts(user.getRemainingPosts() - 1);
        userRepository.save(user);

        return post;
    }


    public Post updatePost(Authentication authentication, Long postId, UpdatePostRequest request) {
        // Lấy user hiện tại
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        // Lấy post cần update
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to update this post", 403);
        }

        if (post.getPostDate().plusDays(limitDays).isBefore(LocalDateTime.now())) {
            throw new BusinessException("You cannot update this post after " + limitDays + " days from posting", 400);
        }

        if (request.getProductType() == ProductType.VEHICLE) {
            if (request.getVehicleBrand() == null || request.getModel() == null || request.getYearOfManufacture() == null) {
                throw new BusinessException("Brand, model, and yearOfManufacture are required for CAR", 400);
            }
        } else if (request.getProductType() == ProductType.BATTERY) {
            if (request.getBatteryType() == null || request.getCapacity() == null || request.getVoltage() == null) {
                throw new BusinessException("BatteryType, capacity, and voltage are required for BATTERY", 400);
            }
        }

        post.setProductType(request.getProductType());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setDeliveryMethods(request.getDeliveryMethods());
        post.setPaymentTypes(request.getPaymentTypes());
        post.setUpdateDate(LocalDateTime.now());
        post.setWeight(request.getWeight());


        if (request.getProductType() == ProductType.VEHICLE) {
            post.setVehicleBrand(request.getVehicleBrand());
            post.setModel(request.getModel());
            post.setYearOfManufacture(request.getYearOfManufacture());
            post.setColor(request.getColor());
            post.setMileage(request.getMileage());
        } else if (request.getProductType() == ProductType.BATTERY) {
            post.setBatteryType(request.getBatteryType());
            post.setCapacity(request.getCapacity());
            post.setVoltage(request.getVoltage());
            post.setBatteryBrand(request.getBatteryBrand());
        }

        // Set status và trusted theo gói
        SellerPackage sellerPackage = null;
        if (user.getSellerPackageId() != null) {
            sellerPackage = sellerPackageRepository.findById(user.getSellerPackageId()).orElse(null);
        }

        if (sellerPackage == null || sellerPackage.getType() == SellerPackageType.BASIC) {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
        } else if (sellerPackage.getType() == SellerPackageType.PREMIUM) {
            post.setStatus(PostStatus.PENDING);
        }

        // Xóa ảnh cũ
        post.getImages().clear();

        // Thêm ảnh mới
        for (String url : request.getImages()) {
            PostImage image = PostImage.builder()
                    .post(post)
                    .imageUrl(url)
                    .build();
            post.getImages().add(image);
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

    public Post getPostById(Authentication authentication, Long postId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to access this post", 403);
        }

        return post;
    }


    public List<Post> getMyPostsByStatus(Authentication authentication, PostStatus status) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        return postRepository.findByUserAndStatus(user, status);
    }

}
