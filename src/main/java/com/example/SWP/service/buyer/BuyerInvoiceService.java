package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.seller.PayInvoiceRequest;
import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.entity.*;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.InvoiceMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.InvoiceRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerInvoiceService {

    UserRepository userRepository;
    ContractRepository contractRepository;
    InvoiceRepository invoiceRepository;
    WalletService walletService;
    NotificationService notificationService;
    SellerOrderDeliveryService sellerOrderDeliveryService;
    InvoiceMapper invoiceMapper;
    ValidateService validateService;

    @NonFinal
    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;

    public void createInvoice(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contract does not exist", 404));

        BigDecimal firstInvoiceAmount;

        if (contract.getOrder().getPaymentType() == PaymentType.FULL) {
            firstInvoiceAmount = contract.getOrder().getPost().getPrice();
        } else if (contract.getOrder().getPaymentType() == PaymentType.DEPOSIT) {
            firstInvoiceAmount = contract.getPrice()
                    .multiply(depositPercentage)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            throw new BusinessException("Unknown payment type", 400);
        }

        Invoice depositInvoice = Invoice.builder()
                .contract(contract)
                .invoiceNumber(Utils.generateCode("IN"))
                .totalPrice(firstInvoiceAmount)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .status(InvoiceStatus.ACTIVE)
                .build();

        invoiceRepository.save(depositInvoice);

        if (contract.getOrder().getPaymentType() == PaymentType.DEPOSIT) {
            BigDecimal finalAmount = contract.getPrice().subtract(firstInvoiceAmount);

            Invoice finalInvoice = Invoice.builder()
                    .contract(contract)
                    .invoiceNumber(Utils.generateCode("IN"))
                    .totalPrice(finalAmount)
                    .createdAt(LocalDateTime.now())
                    .dueDate(null)
                    .status(InvoiceStatus.INACTIVE)
                    .build();

            invoiceRepository.save(finalInvoice);
        }

        String buyerEmail = contract.getOrder().getBuyer().getEmail();
        notificationService.sendNotificationToOneUser(
                buyerEmail,
                "Invoice Created",
                "Dear " + contract.getOrder().getBuyer().getFullName() + ",\n\n" +
                        "Your invoice has been created successfully.\n" +
                        "Contract ID: " + contract.getId() + "\n" +
                        "Payment Type: " + contract.getOrder().getPaymentType() + "\n\n" +
                        "Thank you!"
        );
    }

    public InvoiceResponse getInvoiceDetail(Authentication authentication, Long invoiceId) {
        User user = validateService.validateCurrentUser(authentication);

        Invoice invoice = invoiceRepository.getInvoiceByIdAndContract_Order_Buyer_Id(invoiceId, user.getId());

        if (invoice == null) {
            throw new BusinessException("Invoice does not exist, it could be system issue. Try again", 404);
        }

        return invoiceMapper.toInvoiceResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_Id(user.getId());

        return invoiceMapper.toInvoiceResponseList(list);
    }

    public List<InvoiceResponse> getInvoicesByStatus(Authentication authentication, InvoiceStatus status) {
        User user = validateService.validateCurrentUser(authentication);

        List<Invoice> list = invoiceRepository
                .getInvoiceByContract_Order_Buyer_IdAndStatus(user.getId(), status);

        return invoiceMapper.toInvoiceResponseList(list);
    }

    public void payInvoice(Authentication authentication, PayInvoiceRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Invoice invoice = invoiceRepository.getInvoiceByIdAndContract_Order_Buyer_Id(request.getInvoiceId(), user.getId());
        if (invoice == null) {
            throw new BusinessException("Invoice not found or not belong to you", 404);
        }

        if (invoice.getStatus() == InvoiceStatus.INACTIVE) {
            throw new BusinessException("Invoice is not active", 400);
        }

        if (invoice.getStatus() == InvoiceStatus.EXPIRED) {
            throw new BusinessException("Invoice has expired", 400);
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Invoice already paid", 400);
        }

        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.payWithWallet(
                    user,
                    invoice.getTotalPrice(),
                    invoice.getInvoiceNumber(),
                    Utils.generatePaymentDescription(TransactionType.INVOICE, invoice.getInvoiceNumber()),
                    TransactionType.INVOICE
            );
        } else {
            throw new BusinessException("Unsupported payment method", 400);
        }

        invoice.setPaidAt(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        User seller = invoice.getContract().getOrder().getSeller();
        notificationService.sendNotificationToOneUser(
                seller.getEmail(),
                "Invoice Paid",
                "Invoice #" + invoice.getInvoiceNumber() + " has been paid by the buyer."
        );

        sellerOrderDeliveryService.createDeliveryStatus(invoice.getContract().getOrder());
    }

    public List<InvoiceResponse> getAllInvoicesByOrderId(Authentication authentication, Long orderId) {
        User user = validateService.validateCurrentUser(authentication);

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_IdAndContract_Order_Buyer_Id(orderId, user.getId());

        return invoiceMapper.toInvoiceResponseList(list);
    }
}
