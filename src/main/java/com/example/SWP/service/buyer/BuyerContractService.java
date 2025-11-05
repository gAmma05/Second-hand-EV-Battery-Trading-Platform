package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.user.VerifyContractSignatureRequest;
import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.OtpType;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ContractMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.service.mail.MailService;
import com.example.SWP.service.mail.OtpService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import com.example.SWP.service.validate.ValidateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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

    public void sendContractSignOtp(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Buyer phải là người của contract
        if (!contract.getOrder().getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        // Seller phải ký trước
        if (!contract.isSellerSigned()) {
            throw new BusinessException("Người bán chưa ký hợp đồng", 400);
        }

        // Chi duoc phep ky hop dong dang PENDING
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới được ký", 400);
        }

        if (contract.isBuyerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        // Sinh và gửi OTP
        String otp = otpService.generateAndStoreOtp(user.getEmail(), OtpType.CONTRACT_SIGN);

        mailService.sendOtpEmail(user.getEmail(), otp, OtpType.CONTRACT_SIGN);
    }

    // Buyer xác minh OTP và ký contract
    public void verifyContractSignOtp(Authentication authentication, VerifyContractSignatureRequest request) {

        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        if (!contract.getOrder().getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        if (!contract.isSellerSigned()) {
            throw new BusinessException("Người bán chưa ký hợp đồng", 400);
        }

        if (contract.isBuyerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        otpService.verifyOtp(user.getEmail(), request.getOtp(), OtpType.CONTRACT_SIGN);

        contract.setBuyerSigned(true);
        contract.setBuyerSignedAt(LocalDateTime.now());
        contract.setStatus(ContractStatus.SIGNED);

        contractRepository.save(contract);

        // Tạo hóa đơn sau khi buyer ký
        buyerInvoiceService.createInvoice(contract.getId());

        // Tạo đơn hàng vận chuyển khi là thanh toán FULL
        Order order = contract.getOrder();
        if (order.getPaymentType() == PaymentType.FULL) {
            sellerOrderDeliveryService.createDeliveryStatus(order);
        }

        notificationService.sendNotificationToOneUser(
                contract.getOrder().getSeller().getEmail(),
                "Hợp đồng đã được ký bởi người mua",
                "Người mua đã ký hợp đồng cho đơn hàng #" + contract.getOrder().getId() +
                        ". Vui lòng kiểm tra và xử lý tiếp theo."
        );
    }

    public void cancelContract(Authentication authentication, Long contractId) {
        User buyer = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        if (contract.isBuyerSigned() || contract.getStatus().equals(ContractStatus.SIGNED)) {
            throw new BusinessException("Hợp đồng đã được ký, không thể hủy", 400);
        }

        if (!contract.getOrder().getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không có quyền hủy hợp đồng này", 403);
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới có thể hủy", 400);
        }

        contract.setStatus(ContractStatus.CANCELLED);
        contractRepository.save(contract);

        Order order = contract.getOrder();
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(
                order.getSeller().getEmail(),
                "Hợp đồng đã bị hủy",
                "Người mua đã hủy hợp đồng cho đơn hàng #" + order.getId() +
                        ". Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        User buyer = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        if (!contract.getOrder().getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không có quyền xem hợp đồng này", 403);
        }

        return contractMapper.toContractResponse(contract);
    }

    public List<ContractResponse> getAllContracts(Authentication authentication) {
        User buyer = validateService.validateCurrentUser(authentication);

        List<Contract> contractList = contractRepository.findByOrder_Buyer(buyer);

        return contractMapper.toContractResponses(contractList);
    }
}
