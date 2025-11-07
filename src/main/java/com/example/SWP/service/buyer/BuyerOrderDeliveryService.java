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

    public void confirmReceived(Authentication authentication, Long orderDeliveryId) {
        User user = validateService.validateCurrentUser(authentication);

        OrderDelivery orderDelivery = orderDeliveryRepository.findById(orderDeliveryId).orElseThrow(
                () -> new BusinessException("Đơn hàng vận chuyển không tồn tại", 404)
        );

        Order order = orderDelivery.getOrder();

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xác nhận đơn hàng này", 403);
        }

        if (orderDelivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new BusinessException("Đơn hàng chưa được giao", 400);
        }

        boolean hasActiveInvoice = invoiceRepository.existsByContract_OrderAndStatus(order, InvoiceStatus.ACTIVE);

        if (hasActiveInvoice) {
            throw new BusinessException("Bạn phải thanh toán hóa đơn trước khi xác nhận nhận hàng", 400);
        }

        orderDelivery.setStatus(DeliveryStatus.RECEIVED);
        orderDelivery.setUpdatedAt(LocalDateTime.now());
        orderDeliveryRepository.save(orderDelivery);
    }

    public List<OrderDeliveryResponse> getMyDeliveries(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<OrderDelivery> deliveries = orderDeliveryRepository.findAllByOrder_Buyer_Id(user.getId());

        return orderDeliveryMapper.toOrderDeliveryResponseList(deliveries);
    }

    public OrderDeliveryResponse getDeliveryByOrderId(Authentication authentication, Long orderId) {
        User user = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem thông tin giao hàng của đơn này", 403);
        }

        OrderDelivery delivery = orderDeliveryRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException("Đơn hàng này chưa có thông tin giao hàng", 400));

        return orderDeliveryMapper.toOrderDeliveryResponse(delivery);
    }
}
