package com.example.SWP.service.seller;

import com.example.SWP.dto.request.ghn.FeeRequest;
import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.request.user.VerifyContractSignatureRequest;
import com.example.SWP.dto.response.ghn.FeeResponse;
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
import com.example.SWP.service.ghn.GhnService;
import com.example.SWP.service.mail.MailService;
import com.example.SWP.service.mail.OtpService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    GhnService ghnService;
    ValidateService validateService;
    MailService mailService;
    OtpService otpService;

    @NonFinal
    @Value("${deposit-percentage}")
    BigDecimal depositPercentage;

    public ContractTemplateResponse generateContractTemplate(Authentication authentication, Long orderId) {
        User user = validateService.validateCurrentUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("Order is not approved", 404);
        }

        if (!order.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to create a contract for this order", 403);
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
                .price(post.getPrice());

        if(order.getPaymentType() == PaymentType.DEPOSIT) {
            contractTemplateResponse.depositPercentage(depositPercentage);
        }

        if(post.getProductType() == ProductType.VEHICLE) {
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
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        User user = validateService.validateCurrentUser(authentication);

        User seller = order.getSeller();
        User buyer = order.getBuyer();

        if (seller.getId().equals(buyer.getId())) {
            throw new BusinessException("You can't create contract on your own order", 400);
        }

        if (!user.getId().equals(seller.getId())) {
            throw new BusinessException("You are not the seller of this order", 400);
        }

        if (order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessException("You can't create contract until the order is approved", 400);
        } else if (order.getStatus().equals(OrderStatus.REJECTED)) {
            throw new BusinessException("This order is already rejected, you can no longer create contract on this order", 400);
        } else if(order.getStatus().equals(OrderStatus.DONE)) {
            throw new BusinessException("This order is already done, you can no longer create contract on this order", 400);
        }

        Contract contract = Contract.builder()
                .order(order)
                .contractCode(Utils.generateCode("CT"))
                .sellerSignedAt(LocalDateTime.now())
                .status(ContractStatus.PENDING)
                .build();

        Post post = order.getPost();

        if (order.getDeliveryMethod() == DeliveryMethod.GHN) {

            FeeRequest feeRequest = FeeRequest.builder()
                    .postId(post.getId())
                    .serviceTypeId(order.getServiceTypeId())
                    .build();

            FeeResponse feeResponse = ghnService.calculateShippingFee(feeRequest, buyer);
            contract.setPrice(post.getPrice().add(BigDecimal.valueOf(feeResponse.getTotal())));
        } else {
            contract.setPrice(post.getPrice());
        }

        contractRepository.save(contract);
        notificationService.sendNotificationToOneUser(order.getBuyer().getEmail(), "About your order", "Hey, look like your order's seller has sent the contract, you should check it out.");
    }

    public void sendContractSignOtp(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contract not found", 404));

        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to sign this contract", 403);
        }

        String otp = otpService.generateAndStoreOtp(user.getEmail(), OtpType.CONTRACT_SIGN);

        mailService.sendOtpEmail(user.getEmail(), otp, OtpType.CONTRACT_SIGN);
    }

    public void verifyContractSignOtp(Authentication authentication, VerifyContractSignatureRequest request) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("Contract not found", 404));

        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to sign this contract", 403);
        }

        otpService.verifyOtp(user.getEmail(), request.getOtp(), OtpType.CONTRACT_SIGN);

        contract.setSellerSigned(true);
        contract.setSellerSignedAt(LocalDateTime.now());
        contractRepository.save(contract);

        notificationService.sendNotificationToOneUser(
                contract.getOrder().getBuyer().getEmail(),
                "Contract Signed",
                "The seller has signed the contract. Please check your order."
        );
    }

    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contract does not exist", 404));

        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("This contract does not belong to you", 400);
        }

        return contractMapper.toContractResponse(contract);

    }

    public List<ContractResponse> getAllContracts(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);
        List<Contract> list = contractRepository
                .findByOrder_Seller_Id(user.getId());

        return contractMapper.toContractResponses(list);
    }
}