package com.example.SWP.repository;

import com.example.SWP.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrderByPost_Id(Long postId);
    List<Order> findOrderByBuyer_Id(Long buyerId);
    List<Order> findOrderBySeller_Id(Long sellerId);
}
