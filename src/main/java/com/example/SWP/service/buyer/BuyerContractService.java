package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.user.VerifyContractSignatureRequest;
import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ContractMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.service.escrow.EscrowService;
import com.example.SWP.service.mail.MailService;
import com.example.SWP.service.mail.OtpService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import com.example.SWP.service.user.FeeService;
import com.example.SWP.service.user.WalletService;
import com.example.SWP.service.validate.ValidateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuyerContractService {

    ContractRepository contractRepository;
    NotificationService notificationService;
    OrderRepository orderRepository;
    ValidateService validateService;
    BuyerInvoiceService buyerInvoiceService;
    ContractMapper contractMapper;
    OtpService otpService;
    MailService mailService;
    SellerOrderDeliveryService sellerOrderDeliveryService;
    FeeService feeService;
    WalletService walletService;
    EscrowService escrowService;

    /**
     * Gửi OTP để người mua ký hợp đồng
     */
    public void sendContractSignOtp(Authentication authentication, Long contractId) {
        // Xác thực người mua
        User user = validateService.validateCurrentUser(authentication);

        // Lấy hợp đồng theo ID, nếu không tồn tại thì báo lỗi
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra quyền: chỉ người mua mới được ký
        if (!contract.getOrder().getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        // Chỉ cho phép ký khi người bán đã ký
        if (!contract.isSellerSigned()) {
            throw new BusinessException("Người bán chưa ký hợp đồng", 400);
        }

        // Chỉ cho phép ký khi hợp đồng đang chờ ký
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới được ký", 400);
        }

        // Kiểm tra xem người mua đã ký chưa
        if (contract.isBuyerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        // Tạo OTP và lưu vào hệ thống
        String otp = otpService.generateAndStoreOtp(user.getEmail(), OtpType.CONTRACT_SIGN);

        // Gửi OTP đến email người mua
        mailService.sendOtpEmail(user.getEmail(), otp, OtpType.CONTRACT_SIGN);
    }

    /**
     * Xác thực OTP và ký hợp đồng bởi người mua
     */
    public void verifyContractSignOtp(Authentication authentication, VerifyContractSignatureRequest request) {
        // Xác thực người mua
        User user = validateService.validateCurrentUser(authentication);

        // Lấy hợp đồng theo ID
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra quyền: chỉ người mua mới ký được
        if (!contract.getOrder().getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        // Kiểm tra người bán đã ký chưa
        if (!contract.isSellerSigned()) {
            throw new BusinessException("Người bán chưa ký hợp đồng", 400);
        }

        // Kiểm tra người mua đã ký chưa
        if (contract.isBuyerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        // Xác thực OTP
        otpService.verifyOtp(user.getEmail(), request.getOtp(), OtpType.CONTRACT_SIGN);

        // Lưu trạng thái hợp đồng đã ký
        contract.setBuyerSigned(true);
        contract.setBuyerSignedAt(LocalDateTime.now());
        contract.setStatus(ContractStatus.SIGNED);

        contractRepository.save(contract);

        // Tạo hóa đơn nếu cần
        buyerInvoiceService.createInvoice(contract.getId());

        // Tạo trạng thái giao hàng
        Order order = contract.getOrder();
        sellerOrderDeliveryService.createDeliveryStatus(order);

        // Thông báo cho người bán
        notificationService.sendNotificationToOneUser(
                contract.getOrder().getSeller().getEmail(),
                "Hợp đồng đã được ký bởi người mua",
                "Người mua đã ký hợp đồng cho đơn hàng #" + contract.getOrder().getId() +
                        ". Vui lòng kiểm tra và xử lý tiếp theo."
        );
    }

    /**
     * Hủy hợp đồng bởi người mua (chỉ khi chưa ký)
     */
    public void cancelContract(Authentication authentication, Long contractId) {
        // Xác thực người mua
        User buyer = validateService.validateCurrentUser(authentication);

        // Lấy hợp đồng theo ID
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra hợp đồng chưa ký
        if (contract.isBuyerSigned() || contract.getStatus().equals(ContractStatus.SIGNED)) {
            throw new BusinessException("Hợp đồng đã được ký, không thể hủy", 400);
        }

        // Kiểm tra quyền người mua
        if (!contract.getOrder().getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không có quyền hủy hợp đồng này", 403);
        }

        // Chỉ hủy khi hợp đồng đang chờ ký
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới có thể hủy", 400);
        }

        // Nếu đơn hàng đó đã đặt cọc trước, thì khi hủy hợp đồng sẽ hoàn tiền cọc
        Order order = contract.getOrder();
        Post post = order.getPost();
        if (order.getWantDeposit()) {
            BigDecimal refundAmount = feeService.calculateDepositAmount(post.getPrice());
            walletService.refundToWallet(buyer, refundAmount);
            escrowService.switchStatus(EscrowStatus.REFUND_TO_BUYER, order.getId());

            // Thông báo hoàn cọc
            notificationService.sendNotificationToOneUser(
                    buyer.getEmail(),
                    "Hoàn tiền cọc hợp đồng đã hủy",
                    "Hợp đồng cho đơn hàng #" + order.getId() + " đã bị hủy bởi bạn. Số tiền cọc **" + refundAmount + " VND** đã được hoàn vào ví của bạn."
            );
        }

        // Thay đổi trạng thái hợp đồng và lưu
        contract.setStatus(ContractStatus.CANCELLED);
        contractRepository.save(contract);

        // Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        // Thông báo cho người bán
        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Hợp đồng đã bị hủy",
                "Người mua đã hủy hợp đồng cho đơn hàng #" + order.getId() +
                        ". Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    /**
     * Lấy chi tiết hợp đồng của người mua
     */
    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        // Xác thực người mua
        User buyer = validateService.validateCurrentUser(authentication);

        // Lấy hợp đồng theo ID
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra quyền xem hợp đồng
        if (!contract.getOrder().getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không có quyền xem hợp đồng này", 403);
        }

        // Trả về dữ liệu hợp đồng dạng response
        return contractMapper.toContractResponse(contract);
    }

    /**
     * Lấy danh sách tất cả hợp đồng của người mua
     */
    public List<ContractResponse> getAllContracts(Authentication authentication) {
        // Xác thực người mua
        User buyer = validateService.validateCurrentUser(authentication);

        // Lấy tất cả hợp đồng liên quan đến người mua
        List<Contract> contractList = contractRepository.findByOrder_Buyer(buyer);

        // Trả về danh sách hợp đồng dạng response
        return contractMapper.toContractResponses(contractList);
    }
}
