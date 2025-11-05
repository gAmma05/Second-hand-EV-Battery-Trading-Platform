package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.RejectOrderRequest;
import com.example.SWP.dto.response.user.OrderResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.OrderMapper;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.FeeService;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.service.validate.ValidateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SellerOrderService {

    OrderRepository orderRepository;
    NotificationService notificationService;
    ValidateService validateService;
    OrderMapper orderMapper;

    public OrderResponse getOrderDetail(Authentication authentication, Long orderId) {
        User seller = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        if (!order.getSeller().equals(seller)) {
            throw new BusinessException("Bạn không có quyền xem chi tiết đơn hàng này.", 403);
        }

        return orderMapper.toOrderResponse(order);
    }

    public void approveOrder(Authentication authentication, Long orderId) {
        User seller = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        if (!order.getSeller().equals(seller)) {
            throw new BusinessException("Bạn không có quyền duyệt đơn hàng này", 403);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể duyệt đơn hàng đang chờ duyệt", 400);
        }

        if (orderRepository.existsByPostAndStatus(order.getPost(), OrderStatus.APPROVED)) {
            throw new BusinessException("Bài đăng này đã có đơn hàng được duyệt", 400);
        }

        order.setStatus(OrderStatus.APPROVED);
        orderRepository.save(order);

        List<Order> otherPendingOrders = orderRepository.findAllByPostAndStatus(order.getPost(), OrderStatus.PENDING);
        for (Order otherOrder : otherPendingOrders) {
            if (!otherOrder.getId().equals(order.getId())) {
                otherOrder.setStatus(OrderStatus.REJECTED);
            }
        }
        orderRepository.saveAll(otherPendingOrders);

        notificationService.sendNotificationToOneUser(
                order.getBuyer().getEmail(),
                "Đơn hàng của bạn đã được duyệt",
                "Người bán đã duyệt đơn hàng #" + order.getId() + ". Vui lòng chờ người bán gửi hợp đồng để ký xác nhận."
        );

        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Bạn cần tạo hợp đồng cho đơn hàng #" + order.getId(),
                "Đơn hàng #" + order.getId() + " đã được duyệt. Hãy tạo hợp đồng và gửi cho người mua ký xác nhận."
        );
    }

    public void rejectOrder(Authentication authentication, RejectOrderRequest request) {
        User seller = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        if (!order.getSeller().equals(seller)) {
            throw new BusinessException("Bạn không có quyền từ chối đơn hàng này", 403);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể từ chối đơn hàng đang chờ duyệt", 400);
        }

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(
                order.getBuyer().getEmail(),
                "Đơn hàng của bạn đã bị từ chối",
                "Người bán đã từ chối đơn hàng #" + order.getId() +
                        ". Lý do: " + request.getReason()
        );
    }

    public List<OrderResponse> getMyOrders(Authentication authentication) {
        User seller = validateService.validateCurrentUser(authentication);

        List<Order> results = orderRepository.findOrderBySeller(seller);

        return orderMapper.toOrderResponseList(results);
    }

    public List<OrderResponse> getOrdersByStatus(Authentication authentication, OrderStatus status) {
        User seller = validateService.validateCurrentUser(authentication);

        List<Order> results = orderRepository.findOrderBySellerAndStatus(seller, status);

        return orderMapper.toOrderResponseList(results);
    }
}
