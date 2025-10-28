package com.example.SWP.service.seller;

import com.example.SWP.dto.request.ghn.FeeRequest;
import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.request.seller.SignContractRequest;
import com.example.SWP.dto.response.ghn.FeeResponse;
import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ContractMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.ghn.GhnService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.validate.ValidateService;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerContractService {

    UserRepository userRepository;
    OrderRepository orderRepository;
    ContractRepository contractRepository;
    NotificationService notificationService;
    ContractMapper contractMapper;
    GhnService ghnService;
    ValidateService validateService;

    public void signContract(Authentication authentication, SignContractRequest request) {
        User user = validateService.validateCurrentUser(authentication);
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("Contract does not exist", 404));

        if (!contract.getOrder().getSeller().getId().equals(user.getId())) {
            throw new BusinessException("This contract is not belong to you, you can't sign it", 400);
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("This contract is not pending, you can't sign it", 400);
        }

        contract.setContent(request.getContent());
        contract.setSellerSigned(true);
        contract.setSellerSignedAt(LocalDateTime.now());

        contractRepository.save(contract);

        notificationService.sendNotificationToOneUser(contract.getOrder().getBuyer().getEmail(), "About your order", "Hey, look like your order's seller has sent the contract, you should check it out.");
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
        }

        Contract contract = new Contract();
        contract.setOrder(order);
        contract.setContractCode(Utils.generateCode("CT"));
        contract.setTitle(request.getTitle());
        contract.setCurrency(request.getCurrency());
        contract.setStatus(ContractStatus.PENDING);

        if (order.getDeliveryMethod() == DeliveryMethod.GHN) {
            FeeRequest feeRequest = FeeRequest.builder()
                    .postId(order.getPost().getId())
                    .serviceTypeId(order.getServiceTypeId())
                    .build();

            FeeResponse feeResponse = ghnService.calculateShippingFee(feeRequest, buyer);
            contract.setPrice(request.getPrice().add(BigDecimal.valueOf(feeResponse.getTotal())));
        } else {
            contract.setPrice(request.getPrice());
        }

        contractRepository.save(contract);
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
