package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;

import com.example.SWP.dto.response.OrderDeliveryStatusResponse;
import com.example.SWP.dto.response.buyer.BuyerOrderResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDeliveryStatus;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderDeliveryStatusRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.validator.seller.CreateOrderRequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerOrderService {

    UserRepository userRepository;

    OrderRepository orderRepository;

    PostRepository postRepository;

    NotificationService notificationService;

    CreateOrderRequestValidator createOrderRequestValidator;

    OrderDeliveryStatusRepository orderDeliveryStatusRepository;


    public BuyerOrderResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        OrderDeliveryStatus ods = orderDeliveryStatusRepository.findByOrder_Id(orderId);

        BuyerOrderResponse response = new BuyerOrderResponse();
        response.setOrderId(orderId);
        response.setPostId(order.getPost().getId());
        response.setSellerName(order.getSeller().getFullName());
        response.setPaymentType(order.getPaymentType());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        if (ods != null) {
            response.setDeliveryStatus(getOrderDeliveryStatusResponse(ods));
        } else {
            response.setDeliveryStatus(null);
        }
        return response;
    }

    private OrderDeliveryStatusResponse getOrderDeliveryStatusResponse(OrderDeliveryStatus order) {
        OrderDeliveryStatusResponse ods = new OrderDeliveryStatusResponse();
        ods.setOdsId(order.getId());
        ods.setProvider(order.getDeliveryProvider());
        ods.setTrackingNumber(order.getDeliveryTrackingNumber());
        ods.setStatus(order.getStatus());
        ods.setCreatedAt(order.getCreatedAt());
        ods.setUpdatedAt(order.getUpdatedAt());
        return ods;
    }


    public void createOrder(Authentication authentication, CreateOrderRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("User is not a buyer", 400);
        }

        if (!postRepository.existsById(request.getPostId())) {
            throw new BusinessException("Post does not exist. You cannot create order on this post", 404);
        }

        if (isOrderAvailable(request.getPostId(), OrderStatus.PENDING) || isOrderAvailable(request.getPostId(), OrderStatus.APPROVED)) {
            throw new BusinessException("You or someone have already created an order for this post", 400);
        }

        createOrderRequestValidator.validateInvalid(request);

        Order order = new Order();
        order.setBuyer(user);
        order.setSeller(postRepository.findById(request.getPostId()).orElseThrow().getUser());
        order.setPost(postRepository.findById(request.getPostId()).orElseThrow());
        order.setDeliveryMethod(request.getDeliveryMethod());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentType(request.getPaymentType());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(order.getSeller().getEmail(), "About your post", "Look like someone has created an order for your post, you should check it out.");
    }

    private boolean isOrderAvailable(Long postId, OrderStatus status) {
        List<Order> orderList = orderRepository.findOrderByPost_IdAndStatus(postId, status);
        return orderList != null && !orderList.isEmpty();
    }

    public void cancelOrder(Authentication authentication, CancelOrderRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("User is not a buyer", 400);
        }

        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(() -> new BusinessException("Order does not exist", 404));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("You cannot cancel this order since the seller has approved it, you can cancel it through contract implementation", 400);
        }

        if (!order.getBuyer().equals(user)) {
            throw new BusinessException("This order is not your, you cannot cancel it", 400);
        }

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(order.getSeller().getEmail(), "About your order", "Look like someone has cancelled your order, reason: " + request.getReason() + ". You should check it out.");
    }

    public List<BuyerOrderResponse> getAllOrders(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("User is not a buyer", 400);
        }

        return createList(orderRepository.findOrderByBuyer_Id(user.getId()));
    }

    private List<BuyerOrderResponse> createList(List<Order> orderList) {
        List<BuyerOrderResponse> list = new ArrayList<>();
        for (Order order : orderList) {
            BuyerOrderResponse response = new BuyerOrderResponse();
            response.setOrderId(order.getId());
            response.setPostId(order.getPost().getId());
            response.setSellerName(order.getSeller().getFullName());
            response.setPaymentType(order.getPaymentType());
            response.setPaymentMethod(order.getPaymentMethod());
            response.setStatus(order.getStatus());
            response.setCreatedAt(order.getCreatedAt());
            list.add(response);
        }
        return list;
    }
}
