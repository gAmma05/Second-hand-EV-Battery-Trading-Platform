package com.example.SWP.service.escrow;

import com.example.SWP.entity.Order;
import com.example.SWP.entity.escrow.Escrow;
import com.example.SWP.entity.escrow.EscrowTransaction;
import com.example.SWP.enums.EscrowStatus;
import com.example.SWP.enums.EscrowType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.escrow.EscrowRepository;
import com.example.SWP.repository.escrow.EscrowTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class EscrowService {

    EscrowRepository escrowRepository;

    EscrowTransactionRepository escrowTransactionRepository;

    public void createEscrow(Long sellerId, Long buyerId, Order order, boolean isDeposit, BigDecimal amount) {
        if (sellerId == null || order.getId() == null) {
            throw new BusinessException("ID của seller hoặc đơn hàng bị thiếu", 404);
        }

        Optional<Escrow> escrowOptional = escrowRepository.findByOrder_Id(order.getId());
        Escrow escrow;

        // CASE 1: First time creating escrow -> deposit or payment
        if (escrowOptional.isEmpty()) {

            escrow = Escrow.builder()
                    .buyerId(buyerId)
                    .sellerId(sellerId)
                    .order(order)
                    .depositAmount(isDeposit ? amount : BigDecimal.ZERO)
                    .paymentAmount(isDeposit ? BigDecimal.ZERO : amount)
                    .totalAmount(amount)
                    .status(EscrowStatus.LOCKED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            escrowRepository.save(escrow);

            // Create escrow transaction
            escrowTransactionRepository.save(
                    new EscrowTransaction(
                            null,
                            escrow,
                            null, // no receiver for HOLD
                            amount,
                            isDeposit ? EscrowType.HOLD_DEPOSIT : EscrowType.HOLD_PAYMENT,
                            LocalDateTime.now()
                    )
            );

            return;
        }

        // CASE 2: Escrow exists -> this means it's payment (not deposit)
        escrow = escrowOptional.get();

        if (isDeposit) {
            throw new BusinessException("Bạn đã đặt cọc, không thể tạo escrow lần nữa", 400);
        }

        escrow.setUpdatedAt(LocalDateTime.now());

        // Add to payment amount instead of overriding
        BigDecimal newPayment = escrow.getPaymentAmount().add(amount);
        escrow.setPaymentAmount(newPayment);

        // Update total
        escrow.setTotalAmount(escrow.getDepositAmount().add(newPayment));

        escrowRepository.save(escrow);

        // transaction log
        escrowTransactionRepository.save(
                new EscrowTransaction(
                        null,
                        escrow,
                        null,
                        amount,
                        EscrowType.HOLD_PAYMENT,
                        LocalDateTime.now()
                )
        );
    }

    public void switchStatus(EscrowStatus status, Long orderId) {
        Optional<Escrow> escrowOptional = escrowRepository.findByOrder_Id(orderId);
        if (escrowOptional.isEmpty()) {
            throw new BusinessException("Không tìm thấy escrow", 404);
        }
        Escrow escrow = escrowOptional.get();
        escrow.setStatus(status);
        escrow.setUpdatedAt(LocalDateTime.now());

        if (status == EscrowStatus.REFUND_TO_BUYER) {
            escrowTransactionRepository.save(
                    new EscrowTransaction(
                            null,
                            escrow,
                            escrow.getBuyerId(),
                            escrow.getTotalAmount(),
                            EscrowType.REFUNDED_TO_BUYER,
                            LocalDateTime.now()
                    )
            );
        } else if (status == EscrowStatus.RELEASE_TO_SELLER) {
            escrowTransactionRepository.save(
                    new EscrowTransaction(
                            null,
                            escrow,
                            escrow.getSellerId(),
                            escrow.getTotalAmount(),
                            EscrowType.RELEASED_TO_SELLER,
                            LocalDateTime.now()
                    )
            );
        } else if (status == EscrowStatus.DISPUTED) {
            // Khong lam gi het
        }

        escrowRepository.save(escrow);
    }
}
