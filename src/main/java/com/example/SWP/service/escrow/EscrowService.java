package com.example.SWP.service.escrow;

import com.example.SWP.dto.response.admin.EscrowResponse;
import com.example.SWP.dto.response.admin.EscrowTransactionResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.entity.escrow.Escrow;
import com.example.SWP.entity.escrow.EscrowTransaction;
import com.example.SWP.enums.EscrowStatus;
import com.example.SWP.enums.EscrowType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.repository.escrow.EscrowRepository;
import com.example.SWP.repository.escrow.EscrowTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EscrowService {

    EscrowRepository escrowRepository;

    EscrowTransactionRepository escrowTransactionRepository;
    private final UserRepository userRepository;

    public void createEscrow(Long sellerId, Long buyerId, Order order, boolean isDeposit, BigDecimal amount) {
        log.warn("Creating escrow for buyer {} and seller {} for order {}", buyerId, sellerId, order.getId());
        if (sellerId == null) {
            throw new BusinessException("ID của seller bị thiếu", 404);
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
                    .status(EscrowStatus.LOCKED)
                    .createdAt(LocalDateTime.now())
                    .build();

            escrow.setTotalAmount(escrow.getPaymentAmount().add(escrow.getDepositAmount()));

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

        escrow.setPaymentAmount(amount);

        // Update total
        escrow.setTotalAmount(escrow.getDepositAmount().add(amount));

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

        if (status == EscrowStatus.REFUNDED_TO_BUYER) {
            escrowTransactionRepository.save(
                    new EscrowTransaction(
                            null,
                            escrow,
                            escrow.getBuyerId(),
                            escrow.getTotalAmount(),
                            EscrowType.REFUND_TO_BUYER,
                            LocalDateTime.now()
                    )
            );
        } else if (status == EscrowStatus.RELEASED_TO_SELLER) {
            escrowTransactionRepository.save(
                    new EscrowTransaction(
                            null,
                            escrow,
                            escrow.getSellerId(),
                            escrow.getTotalAmount(),
                            EscrowType.RELEASE_TO_SELLER,
                            LocalDateTime.now()
                    )
            );
        } else if (status == EscrowStatus.DISPUTED) {
            // Khong lam gi het
        }

        escrowRepository.save(escrow);
    }

    public List<EscrowResponse> getEscrowList() {
        List<Escrow> escrowList = escrowRepository.findAll();
        List<EscrowResponse> responseList = new ArrayList<>();
        for (Escrow escrow : escrowList) {
            EscrowResponse escrowResponse = EscrowResponse.builder()
                    .escrowId(escrow.getId())
                    .orderId(escrow.getId())
                    .buyerName(escrow.getOrder().getBuyer().getFullName())
                    .sellerName(escrow.getOrder().getSeller().getFullName())
                    .depositAmount(escrow.getDepositAmount())
                    .paymentAmount(escrow.getPaymentAmount())
                    .totalAmount(escrow.getTotalAmount())
                    .status(escrow.getStatus())
                    .createdAt(escrow.getCreatedAt())
                    .updatedAt(escrow.getUpdatedAt())
                    .build();
            responseList.add(escrowResponse);
        }
        return responseList;
    }

    public List<EscrowTransactionResponse> getEscrowTransactionList() {
        List<EscrowTransaction> escrowTransactionList = escrowTransactionRepository.findAll();
        List<EscrowTransactionResponse> responseList = new ArrayList<>();
        for (EscrowTransaction escrowTransaction : escrowTransactionList) {
            Optional<User> user = null;
            if (escrowTransaction.getReceiverId() == null) {

            } else {
                user = userRepository.findById(escrowTransaction.getReceiverId());
            }
            if (user.isEmpty()) {
                throw new BusinessException("Không tìm thấy người dùng, hãy thử lại", 404);
            }
            Long receiverId = user.get().getId();
            EscrowTransactionResponse escrowTransactionResponse = EscrowTransactionResponse.builder()
                    .etId(escrowTransaction.getId())
                    .escrowId(escrowTransaction.getEscrow().getId())
                    .orderId(escrowTransaction.getEscrow().getId())
                    .receiverName(user.get().getFullName())
                    .amount(escrowTransaction.getAmount())
                    .type(escrowTransaction.getType())
                    .createdAt(escrowTransaction.getCreatedAt())
                    .build();
            responseList.add(escrowTransactionResponse);
        }
        return responseList;
    }
}
