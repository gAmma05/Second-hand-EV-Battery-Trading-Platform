package com.example.SWP.service.seller;

import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.dto.response.seller.RejectOrderResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.enums.SellerPackageType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.*;
import com.example.SWP.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerService {

    UserRepository userRepository;
    PriorityPackageRepository priorityPackageRepository;
    SellerPackageRepository sellerPackageRepository;
    OrderRepository orderRepository;
    NotificationService notificationService;

    public void upgradeToSeller(Authentication authentication, UpgradeToSellerRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        if (user.getRole() == Role.SELLER) {
            throw new BusinessException("User is already a seller", 400);
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new BusinessException("Please update your full name before upgrading to seller", 400);
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new BusinessException("Please update your phone number before upgrading to seller", 400);
        }
        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            throw new BusinessException("Please update your address before upgrading to seller", 400);
        }

        user.setRole(Role.SELLER);
        user.setStoreName(request.getShopName());
        user.setStoreDescription(request.getShopDescription());
        user.setSocialMedia(request.getSocialMedia());
        user.setRemainingPosts(3);
        userRepository.save(user);
    }

    public void approveOrder(Authentication authentication, Long orderId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

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

        sendNotification(buyerEmail, "Order Approved", "Your order has been approved by the admin");
        orderRepository.save(order);
    }

    public void rejectOrder(Authentication authentication, RejectOrderResponse response) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Order order = orderRepository.findById(response.getOrderId())
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

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


    public List<PriorityPackage> getAllPriorityPackages() {
        return priorityPackageRepository.findAll();
    }

    public List<SellerPackage> getAllSellerPackages() {
        return sellerPackageRepository.findAll();
    }
}
