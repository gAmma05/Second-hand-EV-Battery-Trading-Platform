package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerService {

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final PostRepository postRepository;

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

        if (isOrderInPending(request.getPostId())) {
            throw new BusinessException("You or someone have already created an order for this post", 400);
        }

        Order order = new Order();
        order.setBuyer(user);
        order.setSeller(postRepository.findById(request.getPostId()).orElseThrow().getUser());
        order.setPost(postRepository.findById(request.getPostId()).orElseThrow());
        order.setDeliveryMethod(request.getDeliveryMethod());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentType(request.getPaymentType());
        order.setStatus(OrderStatus.PENDING);

        orderRepository.save(order);
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
            throw new BusinessException("Order is not pending", 400);
        }

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
    }

    private boolean isOrderInPending(Long postId) {
        List<Order> orderList = orderRepository.findOrderByPost_Id(postId);
        for (Order order : orderList) {
            if (order.getStatus() == OrderStatus.PENDING) {
                return false;
            }
        }
        return true;
    }

    public List<Order> getAllOrders(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        return orderRepository.findOrderByBuyer_Id(user.getId());
    }
}
