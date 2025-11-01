package com.example.SWP.repository;

import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrderBySeller_Id(Long sellerId);
    List<Order> findOrderBySeller_IdAndStatus(Long sellerId, OrderStatus status);
    List<Order> findOrderByPost_IdAndStatus(Long postId, OrderStatus status);
    List<Order> findOrderByPaymentType(PaymentType paymentType);
    List<Order> findOrderByBuyer(User buyer);
    boolean existsByBuyerAndPostAndStatusNotIn(User buyer, Post post, List<OrderStatus> status);
    List<Order> findOrderByBuyerAndStatus(User buyer, OrderStatus status);
}
