package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.user.VerifyContractSignatureRequest;
import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.OtpType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ContractMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.service.mail.MailService;
import com.example.SWP.service.mail.OtpService;
import com.example.SWP.service.notification.NotificationService;
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

    public void sendContractSignOtp(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contract does not exist", 404));

        // Buyer phải là người của contract
        if (!contract.getOrder().getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to sign this contract", 403);
        }

        // Seller phải ký trước
        if (!contract.isSellerSigned()) {
            throw new BusinessException("Seller has not signed the contract yet", 400);
        }

        // Nếu contract đã ký hoặc hủy thì không cần gửi OTP nữa
        if (contract.getStatus() == ContractStatus.SIGNED) {
            throw new BusinessException("This contract is already signed", 400);
        }
        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new BusinessException("This contract has been cancelled", 400);
        }

        // Sinh và gửi OTP
        String otp = otpService.generateAndStoreOtp(user.getEmail(), OtpType.CONTRACT_SIGN);
        mailService.sendOtpEmail(user.getEmail(), otp, OtpType.CONTRACT_SIGN);
    }

    // Buyer xác minh OTP và ký contract
    public void verifyContractSignOtp(
            Authentication authentication, VerifyContractSignatureRequest request) {

        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("Contract not found", 404));

        if (!contract.getOrder().getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to sign this contract", 403);
        }

        if (!contract.isSellerSigned()) {
            throw new BusinessException("Seller has not signed the contract yet", 400);
        }

        if (contract.isBuyerSigned()) {
            throw new BusinessException("You have already signed this contract", 400);
        }

        otpService.verifyOtp(user.getEmail(), request.getOtp(), OtpType.CONTRACT_SIGN);

        // Cập nhật thông tin hợp đồng
        contract.setBuyerSigned(true);
        contract.setBuyerSignedAt(LocalDateTime.now());
        contract.setStatus(ContractStatus.SIGNED);

        // Tạo hóa đơn sau khi buyer ký
        buyerInvoiceService.createInvoice(contract.getId());

        contractRepository.save(contract);

        // Gửi thông báo cho seller
        String sellerEmail = contract.getOrder().getSeller().getEmail();
        notificationService.sendNotificationToOneUser(
                sellerEmail,
                "Contract Signed by Buyer",
                "The buyer has signed the contract. Please review the invoice and proceed accordingly."
        );
    }

    public void cancelContract(Authentication authentication, Long contractId) {

        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId).orElseThrow(
                () -> new BusinessException("Contract does not exist, it could be system issue. Try again", 404)
        );

        if(contract.isBuyerSigned() || contract.getStatus().equals(ContractStatus.SIGNED)){
            throw new BusinessException("This contract is already signed, you can't cancel it.", 400);
        }

        if(contract.getStatus().equals(ContractStatus.CANCELLED)){
            throw new BusinessException("This contract is already cancelled, you can't cancel it.", 400);
        }

        if(!contract.getOrder().getBuyer().getId().equals(user.getId())){
            throw new BusinessException("This contract is not belong to you, you can't cancel it.", 400);
        }

        contract.setStatus(ContractStatus.CANCELLED);

        contractRepository.save(contract);

        Order order = contract.getOrder();

        order.setStatus(OrderStatus.REJECTED);

        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(contract.getOrder().getSeller().getEmail(), "Your order has been cancelled", "Look like your contract has been cancelled by buyer, you should check it out.");
    }

    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(
                        "Contract does not exist, it could be system issue. Try again", 404));

        if (!contract.getOrder().getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("This contract does not belong to you", 403);
        }

        return contractMapper.toContractResponse(contract);
    }

    public List<ContractResponse> getAllContracts(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);
        List<Contract> contractList = contractRepository
                .findByOrder_Buyer_Id(user.getId());

        return contractMapper.toContractResponses(contractList);
    }
}
