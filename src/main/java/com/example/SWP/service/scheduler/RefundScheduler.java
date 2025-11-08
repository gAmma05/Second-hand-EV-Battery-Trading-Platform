package com.example.SWP.service.scheduler;

import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.service.user.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RefundScheduler {

    OrderDeliveryRepository orderDeliveryRepository;

    OrderRepository orderRepository;

    WalletService walletService;

    @Scheduled(cron = "0 0 0 * * *")
    public void autoRefund() {
        int CHECK_DAYS = 7;
        LocalDateTime today = LocalDateTime.now();
        List<OrderDelivery> odList = orderDeliveryRepository.findByStatus(DeliveryStatus.DELIVERED);
        log.info("Running refund job for {} orders", odList.size());

        try {
            for (OrderDelivery od : odList) {
                if (ChronoUnit.DAYS.between(od.getCreatedAt(), today) >= CHECK_DAYS) {
                    Optional<Order> orderOpt = orderRepository.findById(od.getOrder().getId());
                    if(orderOpt.isEmpty()) {
                       throw new BusinessException("Order not found", 404);
                    }
                    Order order = orderOpt.get();
                    walletService.refundToWallet(order.getSeller(), order.getPost().getPrice());
                }
            }
            log.info("Refunded {} orders", odList.size());
            odList.clear();
            log.info("Clearing order delivery list");
        } catch (Exception e) {
            log.error("Error while processing order {}: {}", odList.getFirst().getId(), e.getMessage());
        }
    }
}
