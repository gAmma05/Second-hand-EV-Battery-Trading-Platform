package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.seller.PayInvoiceRequest;
import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.entity.*;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.InvoiceMapper;
import com.example.SWP.repository.*;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import com.example.SWP.service.user.FeeService;
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

    ContractRepository contractRepository;
    InvoiceRepository invoiceRepository;
    WalletService walletService;
    NotificationService notificationService;
    SellerOrderDeliveryService sellerOrderDeliveryService;
    InvoiceMapper invoiceMapper;
    ValidateService validateService;
    FeeService feeService;
    OrderRepository orderRepository;
    PostRepository postRepository;
    OrderDeliveryRepository orderDeliveryRepository;

    public void createInvoice(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy contract", 404));

        Order order = contract.getOrder();

        BigDecimal firstInvoiceAmount;

        if (order.getPaymentType() == PaymentType.FULL) {
            if(order.isDepositPaid()) {
                firstInvoiceAmount = feeService.calculateRemainingAmount(contract.getTotalFee());
            } else {
                firstInvoiceAmount = contract.getTotalFee();
            }
        } else if (contract.getOrder().getPaymentType() == PaymentType.DEPOSIT) {
            if(order.isDepositPaid()) {
                firstInvoiceAmount = BigDecimal.ZERO;
            } else {
                firstInvoiceAmount = feeService.calculateDepositAmount(contract.getTotalFee());
            }
        } else {
            throw new BusinessException("Loại hình thanh toán không hợp lệ", 400);
        }

        Invoice depositInvoice = Invoice.builder()
                .contract(contract)
                .invoiceNumber(Utils.generateCode("INVOICE"))
                .totalPrice(firstInvoiceAmount)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .status(InvoiceStatus.ACTIVE)
                .build();

        invoiceRepository.save(depositInvoice);

        if (contract.getOrder().getPaymentType() == PaymentType.DEPOSIT) {
            BigDecimal finalAmount = feeService.calculateRemainingAmount(contract.getTotalFee());

            Invoice finalInvoice = Invoice.builder()
                    .contract(contract)
                    .invoiceNumber(Utils.generateCode("INVOICE"))
                    .totalPrice(finalAmount)
                    .createdAt(LocalDateTime.now())
                    .dueDate(null)
                    .status(InvoiceStatus.INACTIVE)
                    .build();

            invoiceRepository.save(finalInvoice);
        }

        notificationService.sendNotificationToOneUser(
                contract.getOrder().getBuyer().getEmail(),
                "Hóa đơn đã được tạo",
                "Hóa đơn hợp đồng #" + contract.getId() + " đã được tạo. Vui lòng kiểm tra trong hệ thống."
        );

    }

    public InvoiceResponse getInvoiceDetail(Authentication authentication, Long invoiceId) {
        User user = validateService.validateCurrentUser(authentication);

        Invoice invoice = invoiceRepository.getInvoiceByIdAndContract_Order_Buyer_Id(invoiceId, user.getId()).orElseThrow(
                () -> new BusinessException("Hóa đơn không tồn tại", 404)
        );

        return invoiceMapper.toInvoiceResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_Id(user.getId());

        return invoiceMapper.toInvoiceResponseList(list);
    }

    public List<InvoiceResponse> getInvoicesByStatus(Authentication authentication, InvoiceStatus status) {
        User user = validateService.validateCurrentUser(authentication);

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_IdAndStatus(user.getId(), status);

        return invoiceMapper.toInvoiceResponseList(list);
    }

    public void payInvoice(Authentication authentication, PayInvoiceRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        Invoice invoice = invoiceRepository
                .getInvoiceByIdAndContract_Order_Buyer_Id(request.getInvoiceId(), user.getId())
                .orElseThrow(() -> new BusinessException("Hóa đơn không tồn tại hoặc có sự cố hệ thống", 404));

        if (invoice.getStatus() == InvoiceStatus.INACTIVE) {
            throw new BusinessException("Hóa đơn chưa kích hoạt", 400);
        }

        if (invoice.getStatus() == InvoiceStatus.EXPIRED) {
            throw new BusinessException("Hóa đơn đã hết hạn", 400);
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Hóa đơn đã được thanh toán", 400);
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
            throw new BusinessException("Phương thức thanh toán không được hỗ trợ", 400);
        }

        invoice.setPaidAt(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        User seller = invoice.getContract().getOrder().getSeller();
        notificationService.sendNotificationToOneUser(
                seller.getEmail(),
                "Hóa đơn đã được thanh toán",
                "Hóa đơn #" + invoice.getInvoiceNumber() + " đã được người mua thanh toán."
        );

        Order order = invoice.getContract().getOrder();

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrder(order).orElse(null);

        if(orderDelivery == null) {
            sellerOrderDeliveryService.createDeliveryStatus(order);
        }
    }


    public List<InvoiceResponse> getAllInvoicesByOrderId(Authentication authentication, Long orderId) {
        User user = validateService.validateCurrentUser(authentication);

        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_IdAndContract_Order_Buyer_Id(orderId, user.getId());

        return invoiceMapper.toInvoiceResponseList(list);
    }
}
