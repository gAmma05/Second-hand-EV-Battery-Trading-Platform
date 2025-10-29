package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.seller.PayInvoiceRequest;
import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.entity.*;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.InvoiceRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import com.example.SWP.service.user.WalletService;
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
import java.util.Optional;

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

    @NonFinal
    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;

    private boolean checkIfInvoiceExist(Long contractId, InvoiceStatus status) {
        return invoiceRepository.findByContractIdAndStatus(contractId, status)
                .isPresent();
    }

    public void createInvoice(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contract does not exist", 404));

        if (checkIfInvoiceExist(contractId, InvoiceStatus.ACTIVE) || checkIfInvoiceExist(contractId, InvoiceStatus.INACTIVE)) {
            throw new BusinessException("You already created the invoice for this order. Please pay!", 400);
        }else if(checkIfInvoiceExist(contractId, InvoiceStatus.PAID)){
            throw new BusinessException("You already paid this order. You can't create invoice again.", 400);
        }

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
                .currency(contract.getCurrency())
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
                    .currency(contract.getCurrency())
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
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("You are not a buyer, you can't use this feature", 400);
        }

        Invoice invoice = invoiceRepository.getInvoiceByIdAndContract_Order_Buyer_Id(invoiceId, user.getId());
        if (invoice == null) {
            throw new BusinessException("Invoice does not exist, it could be system issue. Try again", 404);
        }

        String message;
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(invoice.getDueDate())) {
            message = "Your invoice has expired, please create a new invoice";
            invoice.setStatus(InvoiceStatus.EXPIRED);
        } else {
            message = "Your invoice is still valid, please pay your invoice";
        }

        return new InvoiceResponse(
                invoice.getId(), invoice.getContract().getId(),
                invoice.getInvoiceNumber(), invoice.getTotalPrice(),
                invoice.getCurrency(), invoice.getCreatedAt(),
                invoice.getDueDate(), invoice.getPaidAt(),
                invoice.getStatus(), message
        );
    }

    public List<InvoiceResponse> getAllInvoices(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("You are not a buyer, you can't use this feature", 400);
        }

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_Id(user.getId());

        return getInvoicesList(list);
    }

    public List<InvoiceResponse> getExpiredInvoices(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("You are not a buyer, you can't use this feature", 400);
        }

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_IdAndStatus(user.getId(), InvoiceStatus.EXPIRED);
        return getInvoicesList(list);
    }

    public List<InvoiceResponse> getValidInvoices(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("You are not a buyer, you can't use this feature", 400);
        }

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_IdAndStatus(user.getId(), InvoiceStatus.ACTIVE);
        return getInvoicesList(list);
    }


    private List<InvoiceResponse> getInvoicesList(List<Invoice> list) {
        List<InvoiceResponse> response = new ArrayList<>();
        for (Invoice invoice : list) {
            InvoiceResponse responseInvoice = new InvoiceResponse();
            responseInvoice.setInvoiceId(invoice.getId());
            responseInvoice.setContractId(invoice.getContract().getId());
            responseInvoice.setInvoiceNumber(invoice.getInvoiceNumber());
            responseInvoice.setTotalPrice(invoice.getTotalPrice());
            responseInvoice.setCurrency(invoice.getCurrency());
            responseInvoice.setCreatedAt(invoice.getCreatedAt());
            responseInvoice.setDueDate(invoice.getDueDate());
            responseInvoice.setPaidAt(invoice.getPaidAt());
            responseInvoice.setStatus(invoice.getStatus());
            if (invoice.getStatus() == InvoiceStatus.EXPIRED) {
                responseInvoice.setMessage("This invoice has expired, please create a new invoice");
            } else if (invoice.getStatus() == InvoiceStatus.ACTIVE) {
                responseInvoice.setMessage("This invoice is still valid, please pay your invoice");
            }
            response.add(responseInvoice);
        }
        return response;
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

    public void createFinalInvoiceIfDeposit(Order order) {
        if (order == null) {
            throw new BusinessException("Order not found", 404);
        }

        if (order.getPaymentType() != PaymentType.DEPOSIT) {
            return;
        }

        Contract contract = contractRepository.findByOrder_Id(order.getId())
                .orElseThrow(() -> new BusinessException("Order does not have contract to create final invoice to pay", 400));

        BigDecimal remainingPrice = contract.getPrice()
                .subtract(contract.getPrice()
                        .multiply(BigDecimal.valueOf(100).subtract(depositPercentage))
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));

        Invoice invoice = Invoice.builder()
                .contract(contract)
                .invoiceNumber(Utils.generateCode("IN"))
                .totalPrice(remainingPrice)
                .currency(contract.getCurrency())
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .status(InvoiceStatus.ACTIVE)
                .build();

        invoiceRepository.save(invoice);

        User buyer = contract.getOrder().getBuyer();
        if (buyer != null) {
            String buyerTitle = "Invoice to pay the rest is created";
            String buyerContent = String.format(
                    "Hello %s,\n\nYour invoice to pay the rest is created.\n" +
                            "Invoice code: %s\nAmount payable: %s %s\nDue date: %s\n\nBest Regard,\nSupport team.",
                    buyer.getFullName(),
                    invoice.getInvoiceNumber(),
                    invoice.getTotalPrice(),
                    invoice.getCurrency(),
                    invoice.getDueDate().toLocalDate()
            );

            notificationService.sendNotificationToOneUser(buyer.getEmail(), buyerTitle, buyerContent);
        }
    }
}
