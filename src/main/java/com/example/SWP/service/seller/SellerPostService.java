package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreatePostRequest;
import com.example.SWP.dto.request.seller.UpdatePostRequest;
import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.dto.response.AiValidationResult;
import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.*;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.ProductType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.PostMapper;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.PriorityPackagePaymentRepository;
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
    ValidateService validateService;
    SellerPaymentService sellerPaymentService;
    PriorityPackagePaymentRepository priorityPackagePaymentRepository;
    AiService aiService;
    PostMapper postMapper;
    OrderRepository orderRepository;

    @NonFinal
    @Value("${post.expire.days}")
    int expireDays;

    /**
     * Người bán tao bài đăng
     */
    public PostResponse createPost(Authentication authentication, CreatePostRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        if (request.isWantsTrustedLabel()) {
            if (user.getRemainingPremiumPosts() <= 0) {
                throw new BusinessException(
                        "Bạn không còn lượt đăng bài nhãn TIN CẬY. Vui lòng mua thêm lượt để tiếp tục đăng bài.",
                        400
                );
            }
        } else {
            if (user.getRemainingBasicPosts() <= 0 && user.getRemainingPremiumPosts() <= 0) {
                throw new BusinessException(
                        "Bạn không còn lượt đăng bài nào. Vui lòng mua thêm lượt để tiếp tục đăng bài.",
                        400
                );
            }
        }

        validateService.validateAddressInfo(user);

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

        AiValidationResult result= aiService.validateProduct(aiProductRequest);

        if (!result.isValid()) {
            String errorMsg = result.getReason();
            throw new BusinessException("Bài đăng bị từ chối vì: " + errorMsg, 400);
        }

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
                .wantsTrustedLabel(request.isWantsTrustedLabel())
                .weight(request.getWeight());

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

        if (request.getPriorityPackageId() != null) {
            PriorityPackagePayment payment = sellerPaymentService.priorityPackagePayment(user, request.getPriorityPackageId());
            post.setPriorityPackageId(request.getPriorityPackageId());
            LocalDateTime expireDate = LocalDateTime.now().plusDays(payment.getPriorityPackage().getDurationDays());
            post.setPriorityExpire(expireDate);
            post = postRepository.save(post); // lưu trước khi gán payment
            payment.setPost(post);
            priorityPackagePaymentRepository.save(payment);
        }

        post = postRepository.save(post);

        if (request.isWantsTrustedLabel()) {
            user.setRemainingPremiumPosts(user.getRemainingPremiumPosts() - 1);
        } else {
            if (user.getRemainingBasicPosts() > 0) {
                user.setRemainingBasicPosts(user.getRemainingBasicPosts() - 1);
            } else if (user.getRemainingPremiumPosts() > 0) {
                user.setRemainingPremiumPosts(user.getRemainingPremiumPosts() - 1);
            }
        }

        userRepository.save(user);
        return postMapper.toPostResponse(post);
    }

    /**
     * Người bán cập nhật bài đăng
     */
    public PostResponse updatePost(Authentication authentication, Long postId, UpdatePostRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        validateService.validateAddressInfo(user);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bài đăng", 404));

        if (post.getStatus() == PostStatus.SOLD) {
            throw new BusinessException("Bài đăng hiện đang ở trạng thái đã bán", 400);
        }

        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException("Bài đăng hiện đang ở trạng thái đã xóa", 400);
        }

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền cập nhật bài đăng này", 403);
        }

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

        AiValidationResult result= aiService.validateProduct(aiProductRequest);

        if (!result.isValid()) {
            String errorMsg = result.getReason();
            throw new BusinessException("Bài đăng bị từ chối vì: " + errorMsg, 400);
        }

        post.setProductType(request.getProductType());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setDeliveryMethods(request.getDeliveryMethods());
        post.setPaymentTypes(request.getPaymentTypes());
        post.setUpdateDate(LocalDateTime.now());
        post.setWeight(request.getWeight());
        post.setWantsTrustedLabel(request.isWantsTrustedLabel());

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

        if (request.isWantsTrustedLabel()) {
            post.setTrusted(false);
            post.setStatus(PostStatus.PENDING);
        } else {
            post.setStatus(PostStatus.POSTED);
            post.setTrusted(false);
        }

        Duration durationSinceCreate = Duration.between(post.getPostDate(), LocalDateTime.now());
        boolean isWithin24h = durationSinceCreate.toHours() < 24;

        if (!isWithin24h) {
            if (request.isWantsTrustedLabel()) {
                if (user.getRemainingPremiumPosts() <= 0)
                    throw new BusinessException("Bạn không còn lượt đăng bài Dán Nhãn nào. Vui lòng nâng cấp gói.", 400);
                user.setRemainingPremiumPosts(user.getRemainingPremiumPosts() - 1);
            } else {
                if (user.getRemainingBasicPosts() <= 0 && user.getRemainingPremiumPosts() <= 0)
                    throw new BusinessException(
                            "Bạn không còn lượt đăng bài nào. Vui lòng mua thêm lượt để tiếp tục đăng bài.",
                            400
                    );
                user.setRemainingBasicPosts(user.getRemainingBasicPosts() - 1);
            }
            userRepository.save(user);
        }

        post.getImages().clear();

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
                .orElseThrow(() -> new BusinessException("Không tìm thấy bài đăng", 404));

        if(orderRepository.existsByPost_IdAndStatus(post.getId(), OrderStatus.APPROVED)) {
            throw new BusinessException("Bài đăng này đang có đơn hàng đang xử lý, không thể xóa", 400);
        }

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xóa bài đăng này", 403);
        }

        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException("Bài đăng này đã được xóa trước đó", 400);
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
                .orElseThrow(() -> new BusinessException("Không tìm thấy bài đăng", 404));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền truy cập bài đăng này", 403);
        }

        return postMapper.toPostResponse(post);
    }

    public List<PostResponse> getMyPostsByStatus(Authentication authentication, PostStatus status) {
        User user = validateService.validateCurrentUser(authentication);
        List<Post> results = postRepository.findByUserAndStatus(user, status);
        return postMapper.toPostResponseList(results);
    }

}
