package com.example.SWP.service.scheduler;

import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.escrow.Escrow;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.escrow.EscrowRepository;
import com.example.SWP.service.escrow.EscrowService;
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

    ComplaintRepository complaintRepository;

    EscrowRepository escrowRepository;

    EscrowService escrowService;

    @Scheduled(cron = "0 */1 * * * *")
    public void autoRefund() {
//        int CHECK_DAYS = 7;
        int CHECK_MINUTES = 5;
        LocalDateTime today = LocalDateTime.now();
        List<OrderDelivery> odList = orderDeliveryRepository.findByStatusOrStatus(DeliveryStatus.DELIVERED, DeliveryStatus.RECEIVED);
        log.info("Running refund job for {} orders", odList.size());

        if (odList.isEmpty()) {
            log.info("No orders to process");
            return; // KHÔNG LÀM GÌ CẢ
        }

        for (OrderDelivery od : odList) {
            try {
//                if (ChronoUnit.DAYS.between(od.getCreatedAt(), today) >= CHECK_DAYS) {
                if (ChronoUnit.MINUTES.between(od.getCreatedAt(), today) >= CHECK_MINUTES) {
                    Optional<Order> orderOpt = orderRepository.findById(od.getOrder().getId());
                    if (orderOpt.isEmpty()) {
                        log.warn("Order not found for OrderDelivery {}", od.getId());
                        continue;
                    }
                    Order order = orderOpt.get();
                    if (order.getStatus() == OrderStatus.DONE) {
                        log.warn("Order {} is already done", order.getId());
                        continue;
                    }

                    Optional<Complaint> complaintOptional = complaintRepository.findByOrder_Id(order.getId());
                    if (complaintOptional.isEmpty()) {
                        Optional<Escrow> escrowOptional = escrowRepository.findByOrder_Id(order.getId());
                        if (escrowOptional.isEmpty()) {
                            log.warn("Escrow not found for Order {}", order.getId());
                            continue;
                        }
                        // TODO: logic refund bắt đầu từ đây
                        Escrow escrow = escrowOptional.get();

                        escrowService.switchStatus(EscrowStatus.RELEASED_TO_SELLER, order.getId());
                        walletService.refundToWallet(order.getSeller(), escrow.getTotalAmount());
                        order.getPost().setStatus(PostStatus.SOLD);
                        order.setStatus(OrderStatus.DONE);
                    }
                }
            } catch (Exception e) {
                log.error("Error while processing OrderDelivery {}: {}",
                        od.getId(),
                        e.toString());
            }
        }

        log.info("Refund job finished");
    }
}
