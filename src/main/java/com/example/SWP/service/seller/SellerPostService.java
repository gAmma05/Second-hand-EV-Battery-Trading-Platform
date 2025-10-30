package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.request.seller.UpdatePostRequest;
import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.*;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.ProductType;
import com.example.SWP.enums.SellerPackageType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.PostMapper;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.PriorityPackagePaymentRepository;
import com.example.SWP.repository.SellerPackageRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.ai.AiService;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    AiService aiService;
    private final PostMapper postMapper;

    @NonFinal
    @Value("${post.expire.days}")
    int expireDays;

    public PostResponse createPost(Authentication authentication, CreatePostRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        if (request.isWantsTrustedLabel() && user.getRemainingPremiumPosts() <= 0) {
            throw new BusinessException(
                    "You have no remaining premium post slots for trusted label. Please upgrade your plan.",
                    400
            );
        }

        if(!request.isWantsTrustedLabel() && user.getRemainingBasicPosts() <= 0) {
            throw new BusinessException(
                    "You have no remaining basic post slots. Please upgrade your plan or wait for reset.",
                    400
            );
        }

        // Validate theo productType
        validateService.validatePost(
                request.getProductType(),
                request.getVehicleBrand(),
                request.getModel(),
                request.getYearOfManufacture(),
                request.getColor(),
                request.getMileage(),
                request.getBatteryType(),
                request.getBatteryBrand(),
                request.getCapacity(),
                request.getVoltage()
        );

        // Validate với AI
        AiProductRequest aiProductRequest = AiProductRequest.builder()
                .productType(request.getProductType())
                .vehicleBrand(request.getVehicleBrand())
                .model(request.getModel())
                .yearOfManufacture(request.getYearOfManufacture())
                .color(request.getColor())
                .mileage(request.getMileage())
                .batteryType(request.getBatteryType())
                .capacity(request.getCapacity())
                .voltage(request.getVoltage())
                .batteryBrand(request.getBatteryBrand())
                .build();

        boolean isValid = aiService.validateProduct(aiProductRequest);

        if (!isValid) {
            throw new BusinessException("Product information appears to be incorrect or unrelated. Please verify the details and try again.", 400);
        }

        // Build post
        Post.PostBuilder postBuilder = Post.builder()
                .user(user)
                .productType(request.getProductType())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .address(request.getAddress())
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
        if (request.isWantsTrustedLabel()) {
            post.setStatus(PostStatus.PENDING);

        } else {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
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

        // Lưu post
        post = postRepository.save(post);

        // Cập nhật số lượng post còn lại của user
        if(request.isWantsTrustedLabel()) {
            user.setRemainingPremiumPosts(user.getRemainingPremiumPosts() - 1);
        } else {
            user.setRemainingBasicPosts(user.getRemainingBasicPosts() - 1);
        }
        userRepository.save(user);
        return postMapper.toPostResponse(post);
    }

    public PostResponse updatePost(Authentication authentication, Long postId, UpdatePostRequest request) {
        // Xác thực user
        User user = validateService.validateCurrentUser(authentication);

        // Lấy post cần update
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        // Chỉ cho phép update post có trạng thái POSTED
        if(post.getStatus() != PostStatus.POSTED) {
            throw new BusinessException("Only posts with status POSTED can be updated", 400);
        }

        // Kiểm tra quyền sở hữu post
        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to update this post", 403);
        }

        // Validate theo productType
        validateService.validatePost(
                request.getProductType(),
                request.getVehicleBrand(),
                request.getModel(),
                request.getYearOfManufacture(),
                request.getColor(),
                request.getMileage(),
                request.getBatteryType(),
                request.getBatteryBrand(),
                request.getCapacity(),
                request.getVoltage()
        );

        // Validate với AI
        AiProductRequest aiProductRequest = AiProductRequest.builder()
                .productType(request.getProductType())
                .vehicleBrand(request.getVehicleBrand())
                .model(request.getModel())
                .yearOfManufacture(request.getYearOfManufacture())
                .color(request.getColor())
                .mileage(request.getMileage())
                .batteryType(request.getBatteryType())
                .capacity(request.getCapacity())
                .voltage(request.getVoltage())
                .batteryBrand(request.getBatteryBrand())
                .build();

        boolean isValid = aiService.validateProduct(aiProductRequest);

        if (!isValid) {
            throw new BusinessException("Product information appears to be incorrect or unrelated. Please verify the details and try again.", 400);
        }

        post.setProductType(request.getProductType());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setAddress(request.getAddress());
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
        if (request.isWantsTrustedLabel()) {
            post.setTrusted(false);
            post.setStatus(PostStatus.PENDING);

        } else {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
        }

        // Kiểm tra thời gian tạo post để quyết định có trừ slot hay không
        Duration durationSinceCreate = Duration.between(post.getPostDate(), LocalDateTime.now());
        boolean isWithin24h = durationSinceCreate.toHours() < 24;

        if (!isWithin24h) {
            if (request.isWantsTrustedLabel()) {
                if (user.getRemainingPremiumPosts() <= 0)
                    throw new BusinessException("No remaining premium posts. Please upgrade package.", 400);
                user.setRemainingPremiumPosts(user.getRemainingPremiumPosts() - 1);
            } else {
                if (user.getRemainingBasicPosts() <= 0)
                    throw new BusinessException("No remaining basic posts. Please upgrade package.", 400);
                user.setRemainingBasicPosts(user.getRemainingBasicPosts() - 1);
            }
            userRepository.save(user);
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

        postRepository.save(post);
        return postMapper.toPostResponse(post);
    }


    public void deletePost(Authentication authentication, Long postId) {
        User user = validateService.validateCurrentUser(authentication);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to delete this post", 403);
        }

        if(post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException("Post is already deleted", 400);
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    public List<PostResponse> getMyPosts(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<Post> results = postRepository.findByUserAndStatusNot(user, PostStatus.DELETED);

        return postMapper.toPostResponseList(results);
    }

    public PostResponse getPostById(Authentication authentication, Long postId) {
        User user = validateService.validateCurrentUser(authentication);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to access this post", 403);
        }

        return postMapper.toPostResponse(post);
    }


    public List<PostResponse> getMyPostsByStatus(Authentication authentication, PostStatus status) {
        User user = validateService.validateCurrentUser(authentication);
        List<Post> results = postRepository.findByUserAndStatus(user, status);
        return  postMapper.toPostResponseList(results);
    }

}
