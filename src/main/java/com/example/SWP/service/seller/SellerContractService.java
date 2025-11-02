package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.request.user.VerifyContractSignatureRequest;
import com.example.SWP.dto.response.seller.ContractTemplateResponse;
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
import com.example.SWP.service.mail.MailService;
import com.example.SWP.service.mail.OtpService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerContractService {

    OrderRepository orderRepository;
    ContractRepository contractRepository;
    NotificationService notificationService;
    ContractMapper contractMapper;
    ValidateService validateService;
    MailService mailService;
    OtpService otpService;

    public ContractTemplateResponse generateContractTemplate(Authentication authentication, Long orderId) {
        User user = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("Đơn hàng chưa được duyệt", 400);
        }

        if (!order.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền tạo hợp đồng cho đơn hàng này", 403);
        }

        User buyer = order.getBuyer();
        User seller = order.getSeller();
        Post post = order.getPost();

        ContractTemplateResponse.ContractTemplateResponseBuilder contractTemplateResponse = ContractTemplateResponse.builder()
                .buyerName(buyer.getFullName())
                .buyerAddress(buyer.getAddress())
                .buyerPhone(buyer.getPhone())
                .sellerName(seller.getFullName())
                .sellerAddress(seller.getAddress())
                .sellerPhone(seller.getPhone())
                .productType(post.getProductType())
                .weight(post.getWeight())
                .deliveryMethod(order.getDeliveryMethod())
                .paymentType(order.getPaymentType())
                .price(post.getPrice())
                .shippingFee(order.getShippingFee())
                .depositPercentage(order.getDepositPercentage());

        if (post.getProductType() == ProductType.VEHICLE) {
            contractTemplateResponse.vehicleBrand(post.getVehicleBrand())
                    .model(post.getModel())
                    .yearOfManufacture(post.getYearOfManufacture())
                    .color(post.getColor())
                    .mileage(post.getMileage());
        } else {
            contractTemplateResponse.batteryBrand(post.getBatteryBrand())
                    .batteryType(post.getBatteryType())
                    .capacity(post.getCapacity())
                    .voltage(post.getVoltage());
        }
        return contractTemplateResponse.build();
    }

    public void createContract(Authentication authentication, CreateContractRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        if(contractRepository.existsByOrderAndStatusIn(order, List.of(ContractStatus.PENDING, ContractStatus.SIGNED))) {
            throw new BusinessException("Đơn hàng này đã có hợp đồng, không thể tạo hợp đồng mới", 400);
        }

        User seller = order.getSeller();
        User buyer = order.getBuyer();

        if (seller.getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không thể tạo hợp đồng cho đơn hàng của chính mình", 400);
        }

        if (!user.getId().equals(seller.getId())) {
            throw new BusinessException("Bạn không phải người bán của đơn hàng này", 403);
        }

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("Chỉ đơn hàng đã được duyệt mới có thể tạo hợp đồng", 400);
        }

        Post post = order.getPost();

        Contract contract = Contract.builder()
                .order(order)
                .contractCode(Utils.generateCode("CONTRACT"))
                .content(request.getContent())
                .status(ContractStatus.PENDING)
                .totalFee(post.getPrice().add(order.getShippingFee()))
                .build();

        contractRepository.save(contract);

        notificationService.sendNotificationToOneUser(
                order.getBuyer().getEmail(),
                "Hợp đồng đơn hàng của bạn",
                "Người bán đã gửi hợp đồng cho đơn hàng #" + order.getId() + ". Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    public void sendContractSignOtp(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới được ký", 400);
        }

        if (contract.isSellerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        String otp = otpService.generateAndStoreOtp(user.getEmail(), OtpType.CONTRACT_SIGN);

        mailService.sendOtpEmail(user.getEmail(), otp, OtpType.CONTRACT_SIGN);
    }

    public void verifyContractSignOtp(Authentication authentication, VerifyContractSignatureRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới được ký", 400);
        }

        if (contract.isSellerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        otpService.verifyOtp(user.getEmail(), request.getOtp(), OtpType.CONTRACT_SIGN);

        contract.setSellerSigned(true);
        contract.setSellerSignedAt(LocalDateTime.now());
        contractRepository.save(contract);

        notificationService.sendNotificationToOneUser(
                contract.getOrder().getBuyer().getEmail(),
                "Hợp đồng đã được ký",
                "Người bán đã ký hợp đồng cho đơn hàng #" + contract.getOrder().getId() + ". Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem hợp đồng này", 400);
        }

        return contractMapper.toContractResponse(contract);
    }

    public List<ContractResponse> getAllContracts(Authentication authentication) {
        User seller = validateService.validateCurrentUser(authentication);

        List<Contract> list = contractRepository.findByOrder_Seller(seller);

        return contractMapper.toContractResponses(list);
    }
}