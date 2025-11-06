package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;

import com.example.SWP.dto.request.ghn.FeeRequest;
import com.example.SWP.dto.response.ghn.FeeResponse;
import com.example.SWP.dto.response.user.OrderResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.OrderMapper;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.service.ghn.GhnService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.FeeService;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public void createOrder(Authentication authentication, CreateOrderRequest request) {
        User buyer = validateService.validateCurrentUser(authentication);

        validateService.validateAddressInfo(buyer);

        Post post = postRepository.findById(request.getPostId()).orElseThrow(
                () -> new BusinessException("Bài đăng không tồn tại", 404)
        );

        if (post.getUser().getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không thể tạo đơn hàng cho bài đăng của chính mình", 400);
        }

        if (isOrderAvailable(post.getId(), OrderStatus.APPROVED)) {
            throw new BusinessException("Bài đăng này đã có đơn hàng được duyệt", 400);
        }

        if(orderRepository.existsByPost_IdAndPaymentTypeAndStatusNotIn(post.getId(), PaymentType.DEPOSIT, List.of(OrderStatus.REJECTED))) {
            throw new BusinessException("Bài đăng này đã có đơn hàng được đặt cọc", 400);
        }

        if (isOrderAvailable(post.getId(), OrderStatus.DONE)) {
            throw new BusinessException("Bài đăng này đã hoàn tất giao dịch, không thể tạo đơn hàng mới", 400);
        }

        if (request.getDeliveryMethod() == DeliveryMethod.GHN && request.getServiceTypeId() == null) {
            throw new BusinessException("Vui lòng chọn loại dịch vụ khi sử dụng phương thức giao hàng GHN", 400);
        }

        if(orderRepository.existsByBuyerAndPostAndStatusNotIn(buyer, post, List.of(OrderStatus.REJECTED))) {
            throw new BusinessException("Bạn đã có đơn hàng đang xử lý cho bài đăng này", 400);
        }

        Order.OrderBuilder orderBuilder = Order.builder()
                .buyer(buyer)
                .seller(post.getUser())
                .post(post)
                .deliveryMethod(request.getDeliveryMethod())
                .paymentType(request.getPaymentType())
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PENDING);

        if(request.getPaymentType() == PaymentType.DEPOSIT) {
            orderBuilder.depositPercentage(depositPercentage);
        }

        BigDecimal shippingFee = feeService.calculateShippingFee(post, request.getDeliveryMethod(), request.getServiceTypeId(), buyer);
        Order order = orderBuilder.shippingFee(shippingFee).build();

        if (request.getDeliveryMethod() == DeliveryMethod.GHN) {
            order.setServiceTypeId(request.getServiceTypeId());
        } else {
            order.setServiceTypeId(null);
        }

        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Thông báo về bài đăng của bạn",
                "Có người vừa tạo đơn hàng cho bài đăng của bạn. Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    public void cancelOrder(Authentication authentication, CancelOrderRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        if (!order.getBuyer().equals(user)) {
            throw new BusinessException("Đơn hàng này không thuộc về bạn.", 400);
        }

        if (order.getStatus() == OrderStatus.DONE) {
            throw new BusinessException("Đơn hàng này đã hoàn tất.", 400);
        }

        if (order.getStatus() == OrderStatus.REJECTED) {
            throw new BusinessException("Đơn hàng này đã bị hủy trước đó.", 400);
        }

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Thông báo về đơn hàng của bạn",
                "Người mua đã hủy đơn hàng của bạn. Lý do: " + request.getReason() +
                        ". Vui lòng kiểm tra lại chi tiết trong hệ thống."
        );
    }

    public List<OrderResponse> getMyOrders(Authentication authentication) {
        User buyer = validateService.validateCurrentUser(authentication);

        List<Order> results = orderRepository.findOrderByBuyer(buyer);

        return orderMapper.toOrderResponseList(results);
    }

    public OrderResponse getOrderDetail(Authentication authentication, Long orderId) {
        User buyer = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại.", 404));

        if (!order.getBuyer().equals(buyer)) {
            throw new BusinessException("Bạn không có quyền xem chi tiết đơn hàng này.", 403);
        }

        return orderMapper.toOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByStatus(Authentication authentication, OrderStatus status) {
        User buyer = validateService.validateCurrentUser(authentication);

        List<Order> results = orderRepository.findOrderByBuyerAndStatus(buyer, status);

        return orderMapper.toOrderResponseList(results);
    }

    private boolean isOrderAvailable(Long postId, OrderStatus status) {
        List<Order> orderList = orderRepository.findOrderByPost_IdAndStatus(postId, status);
        return orderList != null && !orderList.isEmpty();
    }

}
