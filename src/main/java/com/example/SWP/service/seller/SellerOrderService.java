package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.OrderDeliveryStatusRequest;
import com.example.SWP.dto.response.OrderDeliveryStatusResponse;
import com.example.SWP.dto.response.seller.SellerOrderResponse;
import com.example.SWP.dto.response.seller.RejectOrderResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDeliveryStatus;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderDeliveryStatusRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SellerOrderService {

    OrderRepository orderRepository;
    NotificationService notificationService;
    UserRepository userRepository;
    OrderDeliveryStatusRepository orderDeliveryStatusRepository;

    public SellerOrderResponse getOrderDetail(Authentication authentication, Long orderId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));
        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        OrderDeliveryStatus ods = orderDeliveryStatusRepository.findByOrder_Id(orderId);

        SellerOrderResponse response = new SellerOrderResponse();
        response.setOrderId(orderId);
        response.setPostId(order.getPost().getId());
        response.setBuyerName(order.getBuyer().getFullName());
        response.setPaymentType(order.getPaymentType());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        if(ods != null){
            response.setDeliveryStatus(getOrderDeliveryStatusResponse(ods));
        }else{
            response.setDeliveryStatus(null);
        }
        return response;
    }

    private OrderDeliveryStatusResponse getOrderDeliveryStatusResponse(OrderDeliveryStatus order){
        OrderDeliveryStatusResponse ods = new OrderDeliveryStatusResponse();
        ods.setOdsId(order.getId());
        ods.setProvider(order.getDeliveryProvider());
        ods.setTrackingNumber(order.getDeliveryTrackingNumber());
        ods.setStatus(order.getStatus());
        ods.setCreatedAt(order.getCreatedAt());
        ods.setUpdatedAt(order.getUpdatedAt());
        return ods;
    }

    public void approveOrder(Authentication authentication, Long orderId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        if(!Objects.equals(order.getSeller().getId(), user.getId())){
            throw new BusinessException("You are not the seller of this order", 400);
        }

        if (order.getStatus().equals(OrderStatus.APPROVED)) {
            throw new BusinessException("Order is already approved", 400);
        } else if (order.getStatus().equals(OrderStatus.REJECTED)) {
            throw new BusinessException("You can't approve this order because it's already rejected", 400);
        } else if (order.getStatus().equals(OrderStatus.PENDING)) {
            order.setStatus(OrderStatus.APPROVED);
        }

        String buyerEmail = order.getBuyer().getEmail();
        if (buyerEmail == null) {
            throw new BusinessException("Buyer email is not found", 404);
        }

        sendNotification(buyerEmail, "Order Approved", "Your order has been approved");
        orderRepository.save(order);
    }

    public void rejectOrder(Authentication authentication, RejectOrderResponse response) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Order order = orderRepository.findById(response.getOrderId())
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        if(!Objects.equals(order.getSeller().getId(), user.getId())){
            throw new BusinessException("You are not the seller of this order", 400);
        }

        if (order.getStatus().equals(OrderStatus.REJECTED)) {
            throw new BusinessException("Order is already rejected", 400);
        } else if (order.getStatus().equals(OrderStatus.APPROVED)) {
            throw new BusinessException("You can't reject this order because it's already approved", 400);
        } else if (order.getStatus().equals(OrderStatus.PENDING)) {
            order.setStatus(OrderStatus.REJECTED);
        }

        String buyerEmail = order.getBuyer().getEmail();
        if (buyerEmail == null) {
            throw new BusinessException("Buyer email is not found", 404);
        }

        sendNotification(buyerEmail, "Order Rejected", "Your order has been rejected with reason: " + response.getReason());
        orderRepository.save(order);
    }

    private void sendNotification(String email, String title, String content) {
        notificationService.sendNotificationToOneUser(email, title, content);
    }

    public List<SellerOrderResponse> getAllOrders(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }
        List<Order> orderList = orderRepository.findOrderBySeller_Id(user.getId());
        return createList(orderList);
    }

    public List<SellerOrderResponse> getPendingOrder(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }

        List<Order> orderList = orderRepository.findOrderBySeller_IdAndStatus(user.getId(), OrderStatus.PENDING);
        return createList(orderList);
    }

    public List<SellerOrderResponse> getRejectedOrder(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }
        List<Order> orderList = orderRepository.findOrderBySeller_IdAndStatus(user.getId(), OrderStatus.REJECTED);
        return createList(orderList);
    }

    public List<SellerOrderResponse> getApprovedOrder(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }
        List<Order> orderList = orderRepository.findOrderBySeller_IdAndStatus(user.getId(), OrderStatus.APPROVED);
        return createList(orderList);
    }

    private List<SellerOrderResponse> createList(List<Order> orderList) {

        List<SellerOrderResponse> responseList = new ArrayList<>();
        for (Order order : orderList) {
            SellerOrderResponse response = new SellerOrderResponse();
            response.setOrderId(order.getId());
            response.setPostId(order.getPost().getId());
            response.setBuyerName(order.getBuyer().getFullName());
            response.setPaymentType(order.getPaymentType());
            response.setStatus(order.getStatus());
            response.setCreatedAt(order.getCreatedAt());
            responseList.add(response);
        }
        return responseList;
    }

    public void updateOrderStatus(OrderDeliveryStatusRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order does not exist"));

        OrderDeliveryStatus ods = orderDeliveryStatusRepository.findByOrder_Id(order.getId());
        if(ods == null){
            throw new BusinessException("Order delivery status does not exist, please try again", 404);
        }

        if(ods.getStatus().equals(DeliveryStatus.PREPARING)){
            ods.setStatus(DeliveryStatus.READY);
        }else if(ods.getStatus().equals(DeliveryStatus.READY)){
            ods.setStatus(DeliveryStatus.PENDING);
        }else if(ods.getStatus().equals(DeliveryStatus.PENDING)){
            ods.setStatus(DeliveryStatus.DELIVERED);
        }

        orderDeliveryStatusRepository.save(ods);
    }
}
