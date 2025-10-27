package com.example.SWP.service.buyer;

import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ContractMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.OrderRepository;
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

    public void signContract(Authentication authentication, Long contractId) {
        User user = validateService.validateCurrentUser(authentication);

        Contract contract = contractRepository.findById(contractId).orElseThrow(
                () -> new BusinessException("Contract does not exist, it could be system issue. Try again", 404)
        );

        ContractStatus status = contract.getStatus();

        if (status == ContractStatus.SIGNED) {
            throw new BusinessException(
                    "This contract is already signed, you cannot sign it again",
                    400
            );
        }

        if (status == ContractStatus.CANCELLED) {
            throw new BusinessException(
                    "This contract has been cancelled, you cannot sign it",
                    400
            );
        }

        if(!contract.isSellerSigned()){
            throw new BusinessException("This contract is not signed by seller yet", 400);
        }

        if(contract.isBuyerSigned()){
            throw new BusinessException("This contract is already signed by buyer", 400);
        }

        contract.setBuyerSigned(true);
        contract.setStatus(ContractStatus.SIGNED);
        contract.setBuyerSignedAt(LocalDateTime.now());

        buyerInvoiceService.createInvoice(contractId);

        // Thông báo cho seller
        String sellerEmail = contract.getOrder().getSeller().getEmail();
        String sellerTitle = "Contract Signed by Buyer";
        String sellerContent = "Your contract has just been signed by the buyer. Please review the invoice and proceed accordingly.";
        notificationService.sendNotificationToOneUser(sellerEmail, sellerTitle, sellerContent);

        contractRepository.save(contract);
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

        contract.setStatus(ContractStatus.CANCELLED);

        contractRepository.save(contract);

        Order order = contract.getOrder();

        order.setStatus(OrderStatus.REJECTED);

        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(contract.getOrder().getSeller().getEmail(), "About your contract", "Look like your contract has been cancelled by buyer, you should check it out.");
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
