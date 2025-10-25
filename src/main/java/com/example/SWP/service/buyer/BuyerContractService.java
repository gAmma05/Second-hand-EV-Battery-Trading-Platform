package com.example.SWP.service.buyer;

import com.example.SWP.dto.response.buyer.ContractResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDeliveryStatus;
import com.example.SWP.entity.User;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.OrderDeliveryStatusRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
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

    OrderDeliveryStatusRepository orderDeliveryStatusRepository;

    public void signContract(Authentication authentication, Long contractId) {
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

        if(contract.getStatus().equals(ContractStatus.SIGNED)
                || contract.getStatus().equals(ContractStatus.CANCELLED)){
            throw new BusinessException("This contract is already signed or cancelled," +
                    " you no longer can sign this contract", 400);
        }

        if(!contract.isSellerSigned()){
            throw new BusinessException("This contract is not signed by seller yet", 400);
        }

        contract.setBuyerSigned(true);
        contract.setStatus(ContractStatus.SIGNED);
        contract.setBuyerSignedAt(LocalDateTime.now());

        OrderDeliveryStatus orderDeliveryStatus = new OrderDeliveryStatus();
        orderDeliveryStatus.setOrder(contract.getOrder());


        if(contract.getOrder().getDeliveryMethod().equals(DeliveryMethod.STANDARD) || contract.getOrder().getDeliveryMethod().equals(DeliveryMethod.EXPRESS)){
            orderDeliveryStatus.setDeliveryProvider(DeliveryProvider.GHN);
        }else{
            orderDeliveryStatus.setDeliveryProvider(DeliveryProvider.NONE);
        }

        orderDeliveryStatus.setDeliveryTrackingNumber(generateDeliveryTrackingCode(contract.getOrder().getDeliveryMethod()));
        orderDeliveryStatus.setCreatedAt(LocalDateTime.now());

        orderDeliveryStatusRepository.save(orderDeliveryStatus);

        notificationService.sendNotificationToOneUser(contract.getOrder().getSeller().getEmail(), "About your contract", "Look like your contract has been signed by buyer, you should check it out.");

        contractRepository.save(contract);
    }

    private String generateDeliveryTrackingCode(DeliveryMethod deliveryMethod){
        String prefix = "DT";
        String timestamp = LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String method = null;
        if(deliveryMethod.equals(DeliveryMethod.STANDARD)){
            method = "ST";
        }else if(deliveryMethod.equals(DeliveryMethod.EXPRESS)){
            method = "EX";
        }else if(deliveryMethod.equals(DeliveryMethod.PICKUP)){
            method = "PI";
        }
        return prefix + "-" + method + "-" + timestamp;
    }

    public void cancelContract(Authentication authentication, Long contractId) {
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

    public List<ContractResponse> getAllContractsSignedBySeller(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("User does not exist", 404)
        );

        if (user.getRole() != Role.BUYER) {
            throw new BusinessException("You can't use this feature", 400);
        }

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
