package com.example.SWP.service.buyer;

import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.User;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.OrderDeliveryMapper;
import com.example.SWP.repository.*;
import com.example.SWP.service.validate.ValidateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuyerOrderDeliveryService {

    ValidateService validateService;
    OrderDeliveryRepository orderDeliveryRepository;
    InvoiceRepository invoiceRepository;
    OrderDeliveryMapper orderDeliveryMapper;
    OrderRepository orderRepository;

    /**
     * Người mua xác nhận đã nhận được hàng
     */
    public void confirmReceived(Authentication authentication, Long orderDeliveryId) {
        // Xác thực và lấy thông tin người mua
        User user = validateService.validateCurrentUser(authentication);

        // Tìm đơn hàng vận chuyển
        OrderDelivery orderDelivery = orderDeliveryRepository.findById(orderDeliveryId).orElseThrow(
                () -> new BusinessException("Đơn hàng vận chuyển không tồn tại", 404)
        );

        Order order = orderDelivery.getOrder();

        // Người dùng phải là người mua của đơn hàng này
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xác nhận đơn hàng này", 403);
        }

        // Chỉ được xác nhận khi đơn đã ở trạng thái "ĐÃ GIAO"
        if (orderDelivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new BusinessException("Đơn hàng chưa được giao", 400);
        }

        // Phải thanh toán hết hóa đơn đang hoạt động
        boolean hasActiveInvoice = invoiceRepository.existsByContract_OrderAndStatus(order, InvoiceStatus.ACTIVE);
        if (hasActiveInvoice) {
            throw new BusinessException("Bạn phải thanh toán hóa đơn trước khi xác nhận nhận hàng", 400);
        }

        // Cập nhật trạng thái
        orderDelivery.setStatus(DeliveryStatus.RECEIVED);
        orderDelivery.setUpdatedAt(LocalDateTime.now());
        orderDeliveryRepository.save(orderDelivery);
    }

    /**
     * Lấy danh sách tất cả các đơn hàng vận chuyển của người mua
     */
    public List<OrderDeliveryResponse> getMyDeliveries(Authentication authentication) {
        // Xác thực và lấy thông tin người dùng
        User user = validateService.validateCurrentUser(authentication);

        // Lấy tất cả đơn hàng giao thuộc về người mua
        List<OrderDelivery> deliveries = orderDeliveryRepository.findAllByOrder_Buyer_Id(user.getId());

        // Chuyển sang DTO
        return orderDeliveryMapper.toOrderDeliveryResponseList(deliveries);
    }

    /**
     * Lấy thông tin vận chuyển của một đơn hàng
     */
    public OrderDeliveryResponse getDeliveryByOrderId(Authentication authentication, Long orderId) {
        // Xác thực và lấy thông tin người dùng
        User user = validateService.validateCurrentUser(authentication);

        // Lấy đơn hàng theo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Người dùng phải là người mua của đơn hàng này
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem thông tin giao hàng của đơn này", 403);
        }

        // Lấy đơn hàng vận chuyển
        OrderDelivery delivery = orderDeliveryRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException("Đơn hàng này chưa có thông tin giao hàng", 400));

        // Chuyển sang DTO
        return orderDeliveryMapper.toOrderDeliveryResponse(delivery);
    }
}
