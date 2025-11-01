package com.example.SWP.repository;

import com.example.SWP.entity.Order;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrderByPost_Id(Long postId);

    List<Order> findOrderByBuyer_Id(Long buyerId);

    List<Order> findOrderBySeller_Id(Long sellerId);

    List<Order> findOrderBySeller_IdAndStatus(Long sellerId, OrderStatus status);

    List<Order> findOrderByPost_IdAndStatus(Long postId, OrderStatus status);

    List<Order> findOrderByPaymentType(PaymentType paymentType);

    Order findByPost_IdAndBuyer_IdAndStatus(Long postId, Long buyerId, OrderStatus status);
}
