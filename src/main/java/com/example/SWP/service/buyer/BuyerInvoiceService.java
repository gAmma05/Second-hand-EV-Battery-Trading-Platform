package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.seller.PayInvoiceRequest;
import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Invoice;
import com.example.SWP.entity.User;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.InvoiceRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
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

    @NonFinal
    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;


    public void createInvoice(Long contractId) {
        if (contractRepository.findById(contractId).isEmpty()) {
            throw new BusinessException("Contract does not exist, it could be system issue. Try again", 404);
        }

        Contract contract = contractRepository.findById(contractId).get();

        BigDecimal totalPrice;

        if (contract.getOrder().getPaymentType() == PaymentType.FULL) {
            totalPrice = contract.getOrder().getPost().getPrice();
        } else if (contract.getOrder().getPaymentType() == PaymentType.DEPOSIT) {
            totalPrice = contract.getPrice()
                    .multiply(depositPercentage)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            throw new BusinessException("Unknown payment type", 400);
        }

        Invoice invoice = Invoice.builder()
                .contract(contract)
                .invoiceNumber(Utils.generateCode("IN"))
                .totalPrice(totalPrice)
                .currency(contract.getCurrency())
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .paidAt(null)
                .status(InvoiceStatus.ACTIVE)
                .build();

        // Thông báo cho buyer
        String buyerEmail = contract.getOrder().getBuyer().getEmail();
        String buyerTitle = "Invoice Created";
        String buyerContent = "Dear " + contract.getOrder().getBuyer().getFullName() + ",\n\n" +
                "Your invoice has been created successfully.\n" +
                "Invoice Number: " + invoice.getInvoiceNumber() + "\n" +
                "Total Price: " + invoice.getTotalPrice() + " " + invoice.getCurrency() + "\n" +
                "Due Date: " + invoice.getDueDate() + "\n\n" +
                "Thank you!";

        notificationService.sendNotificationToOneUser(buyerEmail, buyerTitle, buyerContent);

        invoiceRepository.save(invoice);
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

        if (invoice.getStatus() == InvoiceStatus.EXPIRED) {
            throw new BusinessException("Invoice has expired, please create a new one", 400);
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("This invoice is already paid", 400);
        }

        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.payWithWallet(user, invoice.getTotalPrice(),
                    invoice.getInvoiceNumber(),
                    Utils.generatePaymentDescription(TransactionType.INVOICE, invoice.getInvoiceNumber()),
                    TransactionType.INVOICE);
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setStatus(InvoiceStatus.PAID);
            invoiceRepository.save(invoice);

            User seller = invoice.getContract().getOrder().getSeller();
            String sellerEmail = seller.getEmail();
            String sellerTitle = "Invoice Paid";
            String sellerContent = "Dear " + seller.getFullName() + ",\n\n" +
                    "The invoice #" + invoice.getInvoiceNumber() + " has been paid by the buyer.\n" +
                    "Total Price: " + invoice.getTotalPrice() + " " + invoice.getCurrency() + "\n" +
                    "Paid At: " + invoice.getPaidAt() + "\n\n" +
                    "Please proceed with the next steps of the order.\n\n" +
                    "Thank you!";

            notificationService.sendNotificationToOneUser(sellerEmail, sellerTitle, sellerContent);

        } else {
            throw new BusinessException("Unsupported payment method", 400);
        }
    }
}
