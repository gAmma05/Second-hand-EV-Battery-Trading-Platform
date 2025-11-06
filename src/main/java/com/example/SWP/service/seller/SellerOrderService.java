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

    /**
     * Lấy chi tiết 1 đơn hàng của người bán
     */
    public OrderResponse getOrderDetail(Authentication authentication, Long orderId) {
        // Xác thực và lấy thông tin người bán hiện tại
        User seller = validateService.validateCurrentUser(authentication);

        // Tìm đơn hàng theo ID, nếu không tồn tại thì báo lỗi
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Kiểm tra quyền sở hữu đơn hàng
        if (!order.getSeller().equals(seller)) {
            throw new BusinessException("Bạn không có quyền xem chi tiết đơn hàng này.", 403);
        }

        // Trả về dữ liệu đơn hàng dạng response
        return orderMapper.toOrderResponse(order);
    }

    /**
     * Duyệt một đơn hàng đang chờ duyệt
     * Khi người bán duyệt, các đơn hàng khác cùng bài đăng sẽ bị từ chối
     */
    public void approveOrder(Authentication authentication, Long orderId) {
        // Xác thực người bán hiện tại
        User seller = validateService.validateCurrentUser(authentication);

        // Tìm đơn hàng cần duyệt
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Kiểm tra quyền sở hữu
        if (!order.getSeller().equals(seller)) {
            throw new BusinessException("Bạn không có quyền duyệt đơn hàng này", 403);
        }

        // Chỉ có thể duyệt đơn hàng đang chờ xử lý
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể duyệt đơn hàng đang chờ duyệt", 400);
        }

        // Kiểm tra xem bài đăng đã có đơn hàng được duyệt hay chưa
        if (orderRepository.existsByPostAndStatus(order.getPost(), OrderStatus.APPROVED)) {
            throw new BusinessException("Bài đăng này đã có đơn hàng được duyệt", 400);
        }

        // Cập nhật trạng thái đơn hàng được duyệt
        order.setStatus(OrderStatus.APPROVED);
        orderRepository.save(order);

        // Tự động từ chối các đơn hàng khác đang chờ duyệt cùng bài đăng
        List<Order> otherPendingOrders = orderRepository.findAllByPostAndStatus(order.getPost(), OrderStatus.PENDING);
        for (Order otherOrder : otherPendingOrders) {
            if (!otherOrder.getId().equals(order.getId())) {
                otherOrder.setStatus(OrderStatus.REJECTED);
            }
        }
        orderRepository.saveAll(otherPendingOrders);

        // Gửi thông báo cho người mua
        notificationService.sendNotificationToOneUser(
                order.getBuyer().getEmail(),
                "Đơn hàng của bạn đã được duyệt",
                "Người bán đã duyệt đơn hàng #" + order.getId() +
                        ". Vui lòng chờ người bán gửi hợp đồng để ký xác nhận."
        );

        // Gửi thông báo cho người bán để nhắc tạo hợp đồng
        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Bạn cần tạo hợp đồng cho đơn hàng #" + order.getId(),
                "Đơn hàng #" + order.getId() + " đã được duyệt. Hãy tạo hợp đồng và gửi cho người mua ký xác nhận."
        );
    }

    /**
     * Từ chối một đơn hàng đang chờ duyệt
     */
    public void rejectOrder(Authentication authentication, RejectOrderRequest request) {
        // Xác thực người bán hiện tại
        User seller = validateService.validateCurrentUser(authentication);

        // Tìm đơn hàng theo ID trong request
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Kiểm tra quyền sở hữu đơn hàng
        if (!order.getSeller().equals(seller)) {
            throw new BusinessException("Bạn không có quyền từ chối đơn hàng này", 403);
        }

        // Chỉ được từ chối đơn hàng đang ở trạng thái PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể từ chối đơn hàng đang chờ duyệt", 400);
        }

        // Cập nhật trạng thái đơn hàng sang REJECTED
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        // Gửi thông báo cho người mua biết lý do bị từ chối
        notificationService.sendNotificationToOneUser(
                order.getBuyer().getEmail(),
                "Đơn hàng của bạn đã bị từ chối",
                "Người bán đã từ chối đơn hàng #" + order.getId() +
                        ". Lý do: " + request.getReason()
        );
    }

    /**
     * Lấy toàn bộ đơn hàng của người bán hiện tại
     */
    public List<OrderResponse> getMyOrders(Authentication authentication) {
        // Xác thực người bán đang đăng nhập
        User seller = validateService.validateCurrentUser(authentication);

        // Lấy tất cả đơn hàng mà người bán này là chủ bài đăng
        List<Order> results = orderRepository.findOrderBySeller(seller);

        // Trả về dữ liệu đơn hàng dạng response
        return orderMapper.toOrderResponseList(results);
    }

    /**
     * Lấy danh sách đơn hàng của người bán theo trạng thái cụ thể
     */
    public List<OrderResponse> getOrdersByStatus(Authentication authentication, OrderStatus status) {
        // Xác thực người bán đang đăng nhập
        User seller = validateService.validateCurrentUser(authentication);

        // Tìm danh sách đơn hàng của người bán theo trạng thái
        List<Order> results = orderRepository.findOrderBySellerAndStatus(seller, status);

        // Trả về dữ liệu đơn hàng dạng response
        return orderMapper.toOrderResponseList(results);
    }
}
