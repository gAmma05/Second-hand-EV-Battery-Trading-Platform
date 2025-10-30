package com.example.SWP.service.seller;

import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.DeliveryProvider;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.OrderDeliveryMapper;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.service.ghn.GhnService;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerOrderDeliveryService {

    OrderDeliveryRepository orderDeliveryRepository;
    GhnService ghnService;
    OrderRepository orderRepository;
    OrderDeliveryMapper orderDeliveryMapper;
    ValidateService validateService;

    public void createDeliveryStatus(Order order) {
        if (order == null) {
            throw new BusinessException("Order not found", 404);
        }

        OrderDelivery existing = orderDeliveryRepository.findByOrderId(order.getId());
        if (existing != null) {
            throw new BusinessException("Order already had delivery status", 400);
        }

        OrderDelivery orderDelivery = new OrderDelivery();

        orderDelivery.setOrder(order);
        orderDelivery.setStatus(DeliveryStatus.PREPARING);

        if(order.getDeliveryMethod() == DeliveryMethod.GHN) {
            ghnService.createGhnOrder(orderDelivery);
        }

        orderDelivery.setDeliveryDate(LocalDateTime.now().plusDays(7));
        orderDelivery.setCreatedAt(LocalDateTime.now());
        orderDelivery.setUpdatedAt(LocalDateTime.now());

        orderDeliveryRepository.save(orderDelivery);
    }

    public OrderDeliveryResponse getDeliveryDetail(Authentication authentication, Long orderId) {
        User user = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order không tồn tại", 404));

        if (!order.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to view this order", 403);
        }

        OrderDelivery delivery = orderDeliveryRepository.findByOrderId(orderId);
        if (delivery == null) {
            throw new BusinessException("This order does not have delivery information yet", 404);
        }

        return orderDeliveryMapper.toOrderDeliveryResponse(delivery);
    }

    public List<OrderDeliveryResponse> getMyDeliveries(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<OrderDelivery> deliveries = orderDeliveryRepository.findAllByOrder_Seller_Id(user.getId());
        if (deliveries == null || deliveries.isEmpty()) {
            return List.of();
        }

        return orderDeliveryMapper.toOrderDeliveryResponseList(deliveries);
    }

    public OrderDeliveryResponse updateDeliveryStatus(Long orderId, DeliveryStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found", 404));

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrderId(orderId);
        if (orderDelivery == null) {
            throw new BusinessException("Order doesn't have delivery status yet", 404);
        }

        if (order.getDeliveryMethod() == DeliveryMethod.GHN) {

            if (orderDelivery.getDeliveryProvider() == null && newStatus == DeliveryStatus.READY) {
                ghnService.createOrder(orderDelivery);
                orderDelivery.setDeliveryProvider(DeliveryProvider.GHN);
                orderDelivery.setStatus(DeliveryStatus.READY);
            }

            else if (orderDelivery.getDeliveryProvider() == DeliveryProvider.GHN) {
                DeliveryStatus ghStatus = ghnService.getOrderStatus(orderDelivery.getDeliveryTrackingNumber());
                orderDelivery.setStatus(ghStatus);
            }

            else if (orderDelivery.getDeliveryProvider() == null) {
                orderDelivery.setStatus(newStatus);
            }

            else {
                throw new BusinessException("Cannot update the status after the GHN is created", 400);
            }
        }

        else {
            orderDelivery.setStatus(newStatus);
        }

        orderDelivery.setUpdatedAt(LocalDateTime.now());
        orderDeliveryRepository.save(orderDelivery);
        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }
}
