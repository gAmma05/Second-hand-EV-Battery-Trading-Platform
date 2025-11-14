package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.seller.PayInvoiceRequest;
import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.entity.*;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.InvoiceMapper;
import com.example.SWP.repository.*;
import com.example.SWP.service.escrow.EscrowService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import com.example.SWP.service.user.FeeService;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    OrderDeliveryRepository orderDeliveryRepository;
    EscrowService escrowService;

    /**
     * Tạo hóa đơn cho hợp đồng.
     * - Nếu thanh toán đặt cọc → tạo 2 hóa đơn (đặt cọc + phần còn lại)
     * - Nếu thanh toán toàn bộ → chỉ tạo 1 hóa đơn cuối
     */
    @Transactional
    public void createInvoice(Long contractId) {
        // Lấy hợp đồng, nếu không tồn tại thì báo lỗi
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hợp đồng", 404));

        Order order = contract.getOrder();

        // Tính phần còn lại phải thanh toán
        BigDecimal finalFee;
        Invoice.InvoiceBuilder invoiceBuilder = Invoice.builder()
                .contract(contract)
                .invoiceNumber(Utils.generateCode("FINAL_FEE_INVOICE"))
                .createdAt(LocalDateTime.now())
                .status(InvoiceStatus.INACTIVE); // Chưa kích hoạt

        if (order.getWantDeposit()) {
            finalFee = feeService.calculateRemainingAmount(order.getPost().getPrice(), order.getShippingFee());
        } else {
            finalFee = contract.getTotalFee();
        }

        // Tạo hóa đơn phần còn lại
        Invoice finalInvoice = invoiceBuilder.totalPrice(finalFee).build();
        invoiceRepository.save(finalInvoice);

        // Gửi thông báo cho người mua
        notificationService.sendNotificationToOneUser(
                contract.getOrder().getBuyer().getEmail(),
                "Hóa đơn đã được tạo",
                "Hóa đơn hợp đồng #" + contract.getId() + " đã được tạo. Vui lòng kiểm tra trong hệ thống."
        );
    }


    /**
     * Lấy chi tiết hóa đơn của người mua theo ID hóa đơn.
     */
    public InvoiceResponse getInvoiceDetail(Authentication authentication, Long invoiceId) {
        // Xác thực và lấy thông tin người dùng hiện tại
        User user = validateService.validateCurrentUser(authentication);

        // Tìm hóa đơn theo ID và ID người mua, nếu không có thì báo lỗi
        Invoice invoice = invoiceRepository
                .getInvoiceByIdAndContract_Order_Buyer_Id(invoiceId, user.getId())
                .orElseThrow(() -> new BusinessException("Hóa đơn không tồn tại", 404));

        // Chuyển đổi danh sách entity sang DTO để trả về
        return invoiceMapper.toInvoiceResponse(invoice);
    }


    /**
     * Lấy toàn bộ danh sách hóa đơn của người mua hiện tại.
     */
    public List<InvoiceResponse> getAllInvoices(Authentication authentication) {
        // Xác thực và lấy thông tin người dùng hiện tại
        User user = validateService.validateCurrentUser(authentication);

        // Lấy tất cả hóa đơn thuộc các hợp đồng mà người mua này sở hữu
        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_Id(user.getId());

        // Chuyển đổi danh sách entity sang DTO để trả về
        return invoiceMapper.toInvoiceResponseList(list);
    }

    /**
     * Lấy danh sách hóa đơn của người mua theo trạng thái cụ thể.
     */
    public List<InvoiceResponse> getInvoicesByStatus(Authentication authentication, InvoiceStatus status) {
        // Xác thực và lấy thông tin người dùng hiện tại
        User user = validateService.validateCurrentUser(authentication);

        // Lấy hóa đơn của người mua theo trạng thái được yêu cầu
        List<Invoice> list = invoiceRepository.getInvoiceByContract_Order_Buyer_IdAndStatus(user.getId(), status);

        // Chuyển đổi danh sách entity sang DTO để trả về
        return invoiceMapper.toInvoiceResponseList(list);
    }


    public void payInvoice(Authentication authentication, PayInvoiceRequest request) {
        // Xác thực và lấy thông tin người mua hiện tại
        User user = validateService.validateCurrentUser(authentication);

        // Tìm hóa đơn thuộc về người mua, nếu không tồn tại thì báo lỗi
        Invoice invoice = invoiceRepository
                .getInvoiceByIdAndContract_Order_Buyer_Id(request.getInvoiceId(), user.getId())
                .orElseThrow(() -> new BusinessException("Hóa đơn không tồn tại hoặc có sự cố hệ thống", 404));

        // Kiểm tra trạng thái hóa đơn trước khi thanh toán
        if (invoice.getStatus() == InvoiceStatus.INACTIVE) {
            throw new BusinessException("Hóa đơn chưa kích hoạt", 400);
        }
        if (invoice.getStatus() == InvoiceStatus.EXPIRED) {
            throw new BusinessException("Hóa đơn đã hết hạn", 400);
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Hóa đơn đã được thanh toán", 400);
        }

        // Thực hiện thanh toán qua ví nội bộ
        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.payWithWallet(
                    user,
                    invoice.getTotalPrice(),
                    invoice.getInvoiceNumber(),
                    Utils.generatePaymentDescription(TransactionType.PAY_INVOICE, invoice.getInvoiceNumber()),
                    TransactionType.PAY_INVOICE
            );
        } else {
            // Các phương thức khác chưa được hỗ trợ
            throw new BusinessException("Phương thức thanh toán không được hỗ trợ", 400);
        }

        escrowService.createEscrow(
                invoice.getContract().getOrder().getSeller().getId(),
                user.getId(),
                invoice.getContract().getOrder(),
                false,
                invoice.getTotalPrice());

        // Cập nhật trạng thái hóa đơn sau khi thanh toán thành công
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        // Gửi thông báo cho người bán biết hóa đơn đã được thanh toán
        User seller = invoice.getContract().getOrder().getSeller();
        notificationService.sendNotificationToOneUser(
                seller.getEmail(),
                "Hóa đơn đã được thanh toán",
                "Hóa đơn #" + invoice.getInvoiceNumber() + " đã được người mua thanh toán."
        );

        // Lấy đơn hàng tương ứng để xử lý giao hàng
        Order order = invoice.getContract().getOrder();
        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrder(order).orElse(null);

        if (orderDelivery == null) {
            // Nếu chưa có bản ghi giao hàng → tạo mới
            sellerOrderDeliveryService.createDeliveryStatus(order);
        } else {
            // Chỉ cho phép thanh toán khi đơn đang ở trạng thái "Chờ lấy hàng"
            if (orderDelivery.getStatus() != DeliveryStatus.PICKUP_PENDING) {
                throw new BusinessException("Bạn chỉ có thể thanh toán hóa đơn khi đang chờ lấy hàng", 400);
            }

            // Sau khi thanh toán, cập nhật trạng thái giao hàng thành "Đã giao"
            orderDelivery.setStatus(DeliveryStatus.DELIVERED);
            orderDeliveryRepository.save(orderDelivery);
        }
    }


    /**
     * Lấy danh sách tất cả hóa đơn thuộc về một đơn hàng cụ thể của người mua.
     */
    public List<InvoiceResponse> getAllInvoicesByOrderId(Authentication authentication, Long orderId) {
        // Xác thực và lấy thông tin người mua hiện tại
        User user = validateService.validateCurrentUser(authentication);

        // Lấy danh sách hóa đơn của đơn hàng có ID tương ứng, đảm bảo chỉ lấy hóa đơn thuộc về người mua hiện tại
        List<Invoice> list = invoiceRepository
                .getInvoiceByContract_Order_IdAndContract_Order_Buyer_Id(orderId, user.getId());

        // Chuyển đổi danh sách entity sang DTO để trả về cho client
        return invoiceMapper.toInvoiceResponseList(list);
    }
}
