package com.example.SWP.service.buyer;

import com.example.SWP.dto.response.buyer.ContractResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ContractStatus;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.validate.ValidateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuyerContractService {

    UserRepository userRepository;

    ContractRepository contractRepository;

    NotificationService notificationService;

    OrderRepository orderRepository;

    ValidateService validateService;

    BuyerInvoiceService buyerInvoiceService;

    public void signContract(Long contractId) {

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

        if(!contract.getOrder().getBuyer().getId().equals(user.getId())){
            throw new BusinessException("This contract is not belong to you, you can't cancel it.", 400);
        }

        contract.setStatus(ContractStatus.CANCELLED);

        contractRepository.save(contract);

        Order order = contract.getOrder();

        order.setStatus(OrderStatus.REJECTED);

        orderRepository.save(order);

        notificationService.sendNotificationToOneUser(contract.getOrder().getSeller().getEmail(), "About your contract", "Look like your contract has been cancelled by buyer, you should check it out.");
    }

    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );
        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("You can't use this feature", 400);
        }

        Contract contract = contractRepository.findById(contractId).orElseThrow(
                () -> new BusinessException("Contract does not exist, it could be system issue. Try again", 404)
        );

        ContractResponse response = new ContractResponse();
        response.setContractId(contract.getId());
        response.setOrderId(contract.getOrder().getId());
        response.setContractCode(contract.getContractCode());
        response.setTitle(contract.getTitle());
        response.setContent(contract.getContent());
        response.setPrice(contract.getPrice());
        response.setCurrency(contract.getCurrency());
        response.setStatus(contract.getStatus());

        return response;
    }

    public List<ContractResponse> getAllContract(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );

        List<Contract> contractList = contractRepository.findByOrder_Buyer_Id(user.getId());
        return getContractList(contractList);
    }

    private List<ContractResponse> getContractList(List<Contract> contractList) {
        List<ContractResponse> responseList = new ArrayList<>();
        for (Contract contract : contractList) {
            if (contract.isSellerSigned()) {
                ContractResponse response = new ContractResponse();
                response.setContractId(contract.getId());
                response.setOrderId(contract.getOrder().getId());
                response.setContractCode(contract.getContractCode());
                response.setTitle(contract.getTitle());
                response.setContent(contract.getContent());
                response.setPrice(contract.getPrice());
                response.setCurrency(contract.getCurrency());
                response.setBuyerSigned(contract.isBuyerSigned());
                response.setBuyerSignedAt(contract.getBuyerSignedAt());
                response.setSellerSigned(contract.isSellerSigned());
                response.setSellerSignedAt(contract.getSellerSignedAt());
                response.setStatus(contract.getStatus());
                responseList.add(response);
            }
        }
        return responseList;
    }
}
