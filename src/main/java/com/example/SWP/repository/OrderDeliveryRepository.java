package com.example.SWP.repository;

import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {
    OrderDelivery findByOrderId(Long id);

    Optional<OrderDelivery> findByOrder(Order order);

    List<OrderDelivery> findAllByOrder_Buyer_Id(Long id);

    List<OrderDelivery> findAllByOrder_Seller_Id(Long id);

    List<OrderDelivery> findByStatus(DeliveryStatus status);

    List<OrderDelivery> findByStatusOrStatus(DeliveryStatus status, DeliveryStatus status1);
}
