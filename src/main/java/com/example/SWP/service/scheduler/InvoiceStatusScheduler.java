package com.example.SWP.service.scheduler;

import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Invoice;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.enums.InvoiceStatus;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.InvoiceRepository;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.OrderRepository;
import lombok.AccessLevel;
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
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceStatusScheduler {

    OrderRepository orderRepository;

    OrderDeliveryRepository orderDeliveryRepository;

    ContractRepository contractRepository;

    InvoiceRepository invoiceRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void autoActivateInvoices() {
        int CHECK_DAYS = 7;
        int PAYMENT_DAYS = 7;
        LocalDateTime today = LocalDateTime.now();
        List<Order> orderList = orderRepository.findOrderByPaymentType(PaymentType.DEPOSIT);
        log.info("Running invoice auto-activation job for {} deposit orders", orderList.size());

        for (Order order : orderList) {
            try {
                OrderDelivery orderDelivery = orderDeliveryRepository.findByOrderId(order.getId());
                if (orderDelivery.getDeliveryDate() != null && ChronoUnit.DAYS.between(orderDelivery.getDeliveryDate(), today) >= CHECK_DAYS) {
                    Optional<Contract> optionalContract = contractRepository.findByOrder_Id(order.getId());
                    if (optionalContract.isEmpty()) {
                        continue;
                    }
                    Contract contract = optionalContract.get();

                    Optional<Invoice> optionalInvoice = invoiceRepository.findByContractIdAndStatus(contract.getId(), InvoiceStatus.INACTIVE);
                    if (optionalInvoice.isEmpty()) {
                        continue;
                    }
                    Invoice invoice = optionalInvoice.get();
                    invoice.setStatus(InvoiceStatus.ACTIVE);
                    invoice.setDueDate(today.plusDays(PAYMENT_DAYS));
                    invoiceRepository.save(invoice);

                    log.info("Activated invoice {} for order {} (due date: {})",
                            invoice.getId(), order.getId(), invoice.getDueDate());
                }
            } catch (Exception e) {
                log.error("Error while processing order {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}
