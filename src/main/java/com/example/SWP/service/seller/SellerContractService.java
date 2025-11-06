package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.request.seller.UpdateContractRequest;
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
import org.hibernate.sql.Update;
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

    /**
     * Tạo preview hợp đồng dựa trên đơn hàng
     */
    public ContractTemplateResponse generateContractTemplate(Authentication authentication, Long orderId) {
        // Xác thực người bán
        User user = validateService.validateCurrentUser(authentication);

        // Lấy đơn hàng theo ID, nếu không tồn tại thì báo lỗi
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Chỉ cho phép tạo hợp đồng với đơn hàng đã được duyệt
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("Đơn hàng chưa được duyệt", 400);
        }

        // Kiểm tra quyền của người bán
        if (!order.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền tạo hợp đồng cho đơn hàng này", 403);
        }

        User buyer = order.getBuyer();
        User seller = order.getSeller();
        Post post = order.getPost();

        // Build dữ liệu hợp đồng preview
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

        // Thêm thông tin chi tiết sản phẩm
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

    /**
     * Người bán tạo hợp đồng cho đơn hàng đã được duyệt
     */
    public void createContract(Authentication authentication, CreateContractRequest request) {
        // Xác thực người bán
        User user = validateService.validateCurrentUser(authentication);

        // Lấy đơn hàng theo ID, nếu không tồn tại thì báo lỗi
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Kiểm tra hợp đồng đã tồn tại
        if(contractRepository.existsByOrderAndStatusIn(order, List.of(ContractStatus.PENDING, ContractStatus.SIGNED))) {
            throw new BusinessException("Đơn hàng này đã có hợp đồng, không thể tạo hợp đồng mới", 400);
        }

        User seller = order.getSeller();
        User buyer = order.getBuyer();

        // Kiểm tra người bán không tự tạo hợp đồng cho chính mình
        if (seller.getId().equals(buyer.getId())) {
            throw new BusinessException("Bạn không thể tạo hợp đồng cho đơn hàng của chính mình", 400);
        }

        // Kiểm tra quyền người bán
        if (!user.getId().equals(seller.getId())) {
            throw new BusinessException("Bạn không phải người bán của đơn hàng này", 403);
        }

        // Chỉ tạo hợp đồng cho đơn hàng đã duyệt
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("Chỉ đơn hàng đã được duyệt mới có thể tạo hợp đồng", 400);
        }

        Post post = order.getPost();

        // Build hợp đồng
        Contract contract = Contract.builder()
                .order(order)
                .contractCode(Utils.generateCode("CONTRACT"))
                .content(request.getContent())
                .status(ContractStatus.PENDING) // Chờ ký
                .totalFee(post.getPrice().add(order.getShippingFee()))
                .build();

        // Lưu hợp đồng
        contractRepository.save(contract);

        // Thông báo người mua
        notificationService.sendNotificationToOneUser(
                order.getBuyer().getEmail(),
                "Hợp đồng đơn hàng của bạn",
                "Người bán đã gửi hợp đồng cho đơn hàng #" + order.getId() + ". Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    /**
     * Người bán sửa hợp đồng
     */
    public void updateContract(Authentication authentication, Long contractId, UpdateContractRequest request) {
        // Xác thực người bán
        User user = validateService.validateCurrentUser(authentication);

        // Lấy hợp đồng theo ID, nếu không tồn tại thì báo lỗi
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra quyền truy cập: chỉ người bán mới được sửa
        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền sửa hợp đồng này", 403);
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới được chỉnh sửa", 400);
        }

        // Kiểm tra trạng thái hợp đồng: chỉ sửa khi người bán chưa ký
        if (contract.isSellerSigned()) {
            throw new BusinessException("Bạn không thể sửa hợp đồng đã ký", 400);
        }

        // Kiểm tra trạng thái đơn hàng: chỉ sửa hợp đồng của đơn hàng đã được duyệt
        if (contract.getOrder().getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("Chỉ hợp đồng của đơn hàng đã được duyệt mới được sửa", 400);
        }

        // Cập nhật nội dung hợp đồng
        contract.setContent(request.getContent());

        // Lưu hợp đồng
        contractRepository.save(contract);
    }

    /**
     * Gửi OTP để người bán ký hợp đồng
     */
    public void sendContractSignOtp(Authentication authentication, Long contractId) {
        // Xác thực người bán
        User user = validateService.validateCurrentUser(authentication);

        // Lấy hợp đồng theo ID, nếu không tồn tại thì báo lỗi
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra quyền truy cập: chỉ người bán của đơn hàng mới được ký hợp đồng
        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        // Kiểm tra trạng thái hợp đồng: chỉ hợp đồng đang chờ ký mới được ký
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới được ký", 400);
        }

        // Kiểm tra xem người bán đã ký chưa, nếu đã ký thì không cho ký lại
        if (contract.isSellerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        // Tạo OTP và lưu vào hệ thống
        String otp = otpService.generateAndStoreOtp(user.getEmail(), OtpType.CONTRACT_SIGN);

        // Gửi OTP đến email của người bán
        mailService.sendOtpEmail(user.getEmail(), otp, OtpType.CONTRACT_SIGN);
    }


    /**
     * Người bán ký hợp đồng
     */
    public void verifyContractSignOtp(Authentication authentication, VerifyContractSignatureRequest request) {
        // Xác thực người bán
        User user = validateService.validateCurrentUser(authentication);

        // Lấy hợp đồng theo ID, nếu không tồn tại thì báo lỗi
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra quyền truy cập: chỉ người bán của đơn hàng mới được ký hợp đồng
        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền ký hợp đồng này", 403);
        }

        // Kiểm tra trạng thái hợp đồng: chỉ hợp đồng đang chờ ký mới được ký
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Chỉ hợp đồng đang chờ ký mới được ký", 400);
        }

        // Kiểm tra xem người bán đã ký chưa, nếu đã ký thì không cho ký lại
        if (contract.isSellerSigned()) {
            throw new BusinessException("Bạn đã ký hợp đồng này rồi", 400);
        }

        // Xác thực OTP
        otpService.verifyOtp(user.getEmail(), request.getOtp(), OtpType.CONTRACT_SIGN);

        // Lưu thông tin seller đã ký
        contract.setSellerSigned(true);
        contract.setSellerSignedAt(LocalDateTime.now());
        contractRepository.save(contract);

        // Thông báo cho người mua
        notificationService.sendNotificationToOneUser(
                contract.getOrder().getBuyer().getEmail(),
                "Hợp đồng đã được ký",
                "Người bán đã ký hợp đồng cho đơn hàng #" + contract.getOrder().getId() + ". Vui lòng kiểm tra chi tiết trong hệ thống."
        );
    }

    /**
     * Lấy chi tiết hợp đồng
     */
    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        // Xác thực người bán
        User user = validateService.validateCurrentUser(authentication);

        // Tìm hợp đồng theo ID, nếu không tồn tại thì báo lỗi
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

        // Kiểm tra quyền truy cập: chỉ người bán của đơn hàng mới được xem hợp đồng
        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem hợp đồng này", 400);
        }

        // Trả về dữ liệu hợp đồng dạng response
        return contractMapper.toContractResponse(contract);
    }


    /**
     * Lấy danh sách tất cả hợp đồng của seller
     */
    public List<ContractResponse> getAllContracts(Authentication authentication) {
        // Xác thực người bán
        User seller = validateService.validateCurrentUser(authentication);

        // Lấy tất cả hợp đồng mà seller liên quan
        List<Contract> list = contractRepository.findByOrder_Seller(seller);

        // // Trả về dữ liệu hợp đồng dạng response
        return contractMapper.toContractResponses(list);
    }
}
