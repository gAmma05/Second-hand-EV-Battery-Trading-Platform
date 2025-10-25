package com.example.SWP.service.buyer;

import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Invoice;
import com.example.SWP.entity.User;
import com.example.SWP.entity.wallet.Wallet;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.InvoiceRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.repository.wallet.WalletRepository;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final WalletTransactionRepository walletTransactionRepository;

    public InvoiceResponse createInvoice(Authentication authentication, Long contractId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );

        String message = "Your invoice has been created, please check the information before paying";

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("You are not a buyer, you can't use this feature", 400);
        }

        if (contractRepository.findById(contractId).isEmpty()) {
            throw new BusinessException("Contract does not exist, it could be system issue. Try again", 404);
        }

        if (checkInvoiceIfValid(contractId)) {
            throw new BusinessException("This invoice is still valid, you cannot create new until this invoice is expired", 400);
        }

        Contract contract = contractRepository.findById(contractId).get();

        Invoice invoice = new Invoice();
        invoice.setContract(contract);
        invoice.setInvoiceNumber(Utils.generateCode("IN"));
        invoice.setPaymentMethod(contract.getOrder().getPaymentMethod());
        invoice.setTotalPrice(contract.getOrder().getPost().getPrice());
        invoice.setCurrency("VND");
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(7));
        invoice.setPaidAt(null);
        invoice.setStatus(InvoiceStatus.VALID);

        return new InvoiceResponse(
                invoice.getId(), invoice.getContract().getId(),
                invoice.getInvoiceNumber(), invoice.getTotalPrice(),
                invoice.getCurrency(), invoice.getCreatedAt(),
                invoice.getDueDate(), invoice.getPaidAt(),
                invoice.getStatus(), invoice.getPaymentMethod(),
                message
        );
    }

    private boolean checkInvoiceIfValid(Long contractId) {
        List<Invoice> list = invoiceRepository.getInvoiceByContract_IdAndStatus(contractId, InvoiceStatus.VALID);
        if (list == null || list.isEmpty()) {
            return false;
        }
        return true;
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
                invoice.getStatus(), invoice.getPaymentMethod(),
                message
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

        List<Invoice> list = invoiceRepository.findAll();

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

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_IdAndStatus(user.getId(), InvoiceStatus.VALID);
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
            } else if (invoice.getStatus() == InvoiceStatus.VALID) {
                responseInvoice.setMessage("This invoice is still valid, please pay your invoice");
            }
            response.add(responseInvoice);
        }
        return response;
    }

    public void payInvoice(Authentication authentication, Long invoiceId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        Invoice invoice = invoiceRepository.getInvoiceByIdAndContract_Order_Buyer_Id(invoiceId, user.getId());

        if (invoice == null) {
            throw new BusinessException("Invoice not found or not belong to you", 404);
        }

        if (invoice.getStatus() == InvoiceStatus.EXPIRED) {
            throw new BusinessException("Invoice has expired, please create a new one", 400);
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("This invoice is already paid", 400);
        }

        if(invoice.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.payWithWallet(user, invoice.getTotalPrice(),
                    invoice.getInvoiceNumber(),
                    Utils.generatePaymentDescription(TransactionType.INVOICE, invoice.getInvoiceNumber()),
                    TransactionType.INVOICE);
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setStatus(InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
        } else {
            throw new BusinessException("Unsupported payment method", 400);
        }
    }
}
