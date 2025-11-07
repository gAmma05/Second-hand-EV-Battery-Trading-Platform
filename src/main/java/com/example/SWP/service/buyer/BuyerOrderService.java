package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.response.user.OrderResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.OrderMapper;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.FeeService;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerOrderService {

    OrderRepository orderRepository;
    PostRepository postRepository;
    NotificationService notificationService;
    ValidateService validateService;
    OrderMapper orderMapper;
    FeeService feeService;

    @NonFinal
    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;

    /**
     * Người mua tạo đơn hàng mới
     */
    @Transactional
    public void createOrder(Authentication authentication, CreateOrderRequest request) {
        // Xác thực và lấy thông tin người mua hiện tại
        User buyer = validateService.validateCurrentUser(authentication);

        // Kiểm tra xem người mua đã có đủ thông tin địa chỉ hay chưa
        validateService.validateAddressInfo(buyer);

        // Lấy thông tin bài đăng mà người mua muốn đặt hàng
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException("Bài đăng không tồn tại", 404));

        // Kiểm tra loại thanh toán mà bài đăng hỗ trợ
        if (!post.getPaymentTypes().contains(request.getPaymentType())) {
            throw new BusinessException("Bài đăng không hỗ trợ loại thanh toán này", 400);
        }

        // Kiểm tra loại giao hàng mà bài đăng hỗ trợ
        if(!post.getDeliveryMethods().contains(request.getDeliveryMethod())) {
            throw new BusinessException("Bài đăng không hỗ trợ loại giao hàng này", 400);
        }

        // Nếu chọn giao hàng GHN mà không có loại dịch vụ cụ thể thì báo lỗi
        if (request.getDeliveryMethod() == DeliveryMethod.GHN && request.getServiceTypeId() == null) {
            throw new BusinessException("Vui lòng chọn loại dịch vụ khi sử dụng phương thức giao hàng GHN", 400);
        }

        // Không cho phép người bán tự mua bài đăng của chính mình
        if (post.getUser().getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không thể tạo đơn hàng cho bài đăng của chính mình", 400);
        }

        // Nếu bài đăng đã có đơn hàng được duyệt, không cho tạo thêm
        if (orderRepository.existsByPost_IdAndStatus(post.getId(), OrderStatus.APPROVED)) {
            throw new BusinessException("Bài đăng này đã có đơn hàng được duyệt", 400);
        }

        // Nếu bài đăng đã hoàn tất giao dịch thì không thể đặt hàng nữa
        if (orderRepository.existsByPost_IdAndStatus(post.getId(), OrderStatus.DONE)) {
            throw new BusinessException("Bài đăng này đã hoàn tất giao dịch", 400);
        }

        // Nếu đã có đơn đặt cọc đang chờ xử lý thì không được tạo thêm đơn đặt cọc mới
        if (orderRepository.existsByPost_IdAndPaymentTypeAndStatus(
                post.getId(),
                PaymentType.DEPOSIT,
                OrderStatus.PENDING)) {
            throw new BusinessException("Bài đăng này đã có đơn hàng được đặt cọc", 400);
        }

        // Nếu người mua có một đơn hàng đang chờ xử lý cho cùng bài đăng, không cho phép tạo thêm
        if (orderRepository.existsByBuyer_IdAndPost_IdAndStatus(
                buyer.getId(),
                post.getId(),
                OrderStatus.PENDING)) {
            throw new BusinessException("Bạn đã có đơn hàng đang xử lý cho bài đăng này", 400);
        }

        // Khởi tạo đối tượng Order
        Order.OrderBuilder orderBuilder = Order.builder()
                .buyer(buyer)
                .seller(post.getUser())
                .post(post)
                .deliveryMethod(request.getDeliveryMethod())
                .paymentType(request.getPaymentType())
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PENDING);

        // Nếu là đơn đặt cọc thì gán tỷ lệ đặt cọc
        if (request.getPaymentType() == PaymentType.DEPOSIT) {
            orderBuilder.depositPercentage(depositPercentage);
        }

        // Tính phí vận chuyển dựa trên phương thức giao hàng và địa chỉ người mua
        BigDecimal shippingFee = feeService.calculateShippingFee(
                post,
                request.getDeliveryMethod(),
                request.getServiceTypeId(),
                buyer
        );

        Order order = orderBuilder.shippingFee(shippingFee).build();

        // Nếu chọn GHN thì lưu loại dịch vụ, ngược lại để null
        if (request.getDeliveryMethod() == DeliveryMethod.GHN) {
            order.setServiceTypeId(request.getServiceTypeId());
        } else {
            order.setServiceTypeId(null);
        }

        // Lưu đơn hàng vào cơ sở dữ liệu
        orderRepository.save(order);

        // Gửi thông báo cho người bán biết có đơn hàng mới
        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Thông báo về bài đăng của bạn",
                "Có người vừa tạo đơn hàng cho bài đăng của bạn. Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    /**
     * Người mua hủy đơn hàng
     */
    public void cancelOrder(Authentication authentication, CancelOrderRequest request) {
        // Xác thực và lấy thông tin người mua hiện tại
        User user = validateService.validateCurrentUser(authentication);

        // Kiểm tra xem đơn hàng có tồn tại hay không
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Chỉ cho phép người mua hủy đơn hàng của chính họ
        if (!order.getBuyer().equals(user)) {
            throw new BusinessException("Đơn hàng này không thuộc về bạn.", 400);
        }

        // Chỉ có thể hủy đơn hàng đang chờ xử lý
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể hủy đơn hàng đang chờ duyệt", 400);
        }

        // Cập nhật trạng thái đơn hàng thành CANCELED
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        // Gửi thông báo cho người bán biết rằng người mua đã hủy đơn hàng
        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Thông báo về đơn hàng của bạn",
                String.format("Người mua đã hủy đơn hàng của bạn. Lý do: %s. Vui lòng kiểm tra lại chi tiết trong hệ thống.", request.getReason())
        );
    }

    /**
     * Lấy tất cả đơn hàng của người mua
     */
    public List<OrderResponse> getMyOrders(Authentication authentication) {
        // Xác thực và lấy thông tin người mua hiện tại
        User buyer = validateService.validateCurrentUser(authentication);

        // Lấy danh sách tất cả đơn hàng mà người mua này đã tạo
        List<Order> results = orderRepository.findOrderByBuyer(buyer);

        // Trả về dữ liệu đơn hàng dạng response
        return orderMapper.toOrderResponseList(results);
    }

    /**
     * Xem chi tiết một đơn hàng cụ thể
     */
    public OrderResponse getOrderDetail(Authentication authentication, Long orderId) {
        // Xác thực và lấy thông tin người mua hiện tại
        User buyer = validateService.validateCurrentUser(authentication);

        // Tìm đơn hàng theo ID, nếu không tồn tại thì báo lỗi
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại.", 404));

        // Chỉ cho phép người mua xem đơn hàng thuộc về họ
        if (!order.getBuyer().equals(buyer)) {
            throw new BusinessException("Bạn không có quyền xem chi tiết đơn hàng này.", 403);
        }

        // Trả về dữ liệu đơn hàng dạng response
        return orderMapper.toOrderResponse(order);
    }

    /**
     * Lọc danh sách đơn hàng theo trạng thái
     */
    public List<OrderResponse> getOrdersByStatus(Authentication authentication, OrderStatus status) {
        // Xác thực và lấy thông tin người mua hiện tại
        User buyer = validateService.validateCurrentUser(authentication);

        // Lấy danh sách đơn hàng của người mua theo trạng thái chỉ định (PENDING, APPROVED, DONE, ...)
        List<Order> results = orderRepository.findOrderByBuyerAndStatus(buyer, status);

        // Trả về dữ liệu đơn hàng dạng response
        return orderMapper.toOrderResponseList(results);
    }
}
