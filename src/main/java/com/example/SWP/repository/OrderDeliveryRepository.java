package com.example.SWP.repository;

import com.example.SWP.entity.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {
    OrderDelivery findByOrderId(Long id);

    List<OrderDelivery> findAllByOrder_Buyer_Id(Long id);

    List<OrderDelivery> findAllByOrder_Seller_Id(Long id);
}
