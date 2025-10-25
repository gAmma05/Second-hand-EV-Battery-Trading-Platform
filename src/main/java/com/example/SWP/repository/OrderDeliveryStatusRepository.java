package com.example.SWP.repository;

import com.example.SWP.entity.OrderDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDeliveryStatusRepository extends JpaRepository<OrderDeliveryStatus, Long> {
    OrderDeliveryStatus findByOrder_Id(Long orderId);
}
