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
            throw new BusinessException("Order không tồn tại", 404);
        }

        OrderDelivery existing = orderDeliveryRepository.findByOrderId(order.getId());
        if (existing != null) {
            throw new BusinessException("Order đã có trạng thái giao hàng", 400);
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
            throw new BusinessException("Bạn không có quyền xem đơn hàng này", 403);
        }

        OrderDelivery delivery = orderDeliveryRepository.findByOrderId(orderId);
        if (delivery == null) {
            throw new BusinessException("Đơn hàng chưa có thông tin vận chuyển", 404);
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

    public OrderDeliveryResponse updateManualDeliveryStatus(Long id, DeliveryStatus newStatus) {
        OrderDelivery orderDelivery = orderDeliveryRepository.findById(id).orElseThrow(
                () -> new BusinessException("Đơn hàng không tồn tại", 404)
        );

        if (orderDelivery.getDeliveryProvider() == DeliveryProvider.GHN) {
            throw new BusinessException("Không thể cập nhật trạng thái thủ công cho đơn GHN", 400);
        }

        if (orderDelivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new BusinessException("Không thể cập nhật trạng thái cho đơn đã giao", 400);
        }

        orderDelivery.setStatus(newStatus);
        orderDelivery.setUpdatedAt(LocalDateTime.now());

        orderDeliveryRepository.save(orderDelivery);

        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }


    public OrderDeliveryResponse updateGhnDeliveryStatus(Long id) {
        OrderDelivery orderDelivery = orderDeliveryRepository.findById(id).orElseThrow(
                () -> new BusinessException("Đơn hàng không tồn tại", 404)
        );

        if (orderDelivery.getDeliveryProvider() != DeliveryProvider.GHN) {
            throw new BusinessException("Chỉ có thể cập nhật trạng thái đơn GHN", 400);
        }

        DeliveryStatus ghnStatus = ghnService.getOrderStatus(orderDelivery.getDeliveryTrackingNumber());
        orderDelivery.setStatus(ghnStatus);

        orderDelivery.setUpdatedAt(LocalDateTime.now());
        orderDeliveryRepository.save(orderDelivery);

        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }

}
