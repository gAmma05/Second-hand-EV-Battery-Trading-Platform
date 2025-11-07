package com.example.SWP.repository;

import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByPost_IdAndPaymentTypeAndStatus(Long postId, PaymentType paymentType, OrderStatus status);
    List<Order> findOrderByBuyer(User buyer);
    boolean existsByBuyer_IdAndPost_IdAndStatus(Long buyerId, Long postId, OrderStatus status);
    List<Order> findOrderByBuyerAndStatus(User buyer, OrderStatus status);
    boolean existsByPost_IdAndStatus(Long postId, OrderStatus orderStatus);
    List<Order> findOrderBySeller(User seller);
    List<Order> findOrderBySellerAndStatus(User seller, OrderStatus orderStatus);
    long countBySellerAndStatus(User seller, OrderStatus status);
    long countBySellerAndStatusAndCreatedAtBetween(User seller, OrderStatus status, LocalDateTime start, LocalDateTime end);
    List<Order> findAllByPost_IdAndStatus(Long postId, OrderStatus orderStatus);
    int countOrderByStatus(OrderStatus orderStatus);
}
