package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.response.PreContractResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerContractService {

    UserRepository userRepository;
    OrderRepository orderRepository;
    ContractRepository contractRepository;
    NotificationService notificationService;

    public PreContractResponse getPreContractByOrderId(Authentication authentication, Long orderId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }


        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        if (order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessException("You can't create contract until the order is approved", 400);
        } else if (order.getStatus().equals(OrderStatus.REJECTED)) {
            throw new BusinessException("This order is already rejected, you can no longer create contract on this order", 400);
        }

        PreContractResponse response = new PreContractResponse();
        response.setOrderId(orderId);
        response.setTitle(">" + order.getPost().getTitle() + "<");
        response.setPrice(order.getPost().getPrice());
        response.setCurrency("VND");

        return response;

    }

    private String generateContractCode() {
        String prefix = "CT";
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + timestamp;
    }

    public void createContract(Authentication authentication, CreateContractRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("Order does not exist", 404));

        if (order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessException("You can't create contract until the order is approved", 400);
        } else if (order.getStatus().equals(OrderStatus.REJECTED)) {
            throw new BusinessException("This order is already rejected, you can no longer create contract on this order", 400);
        }

        Contract contract = new Contract();
        contract.setOrder(order);
        contract.setContractCode(generateContractCode());
        contract.setTitle(request.getTitle());
        contract.setContent(request.getContent());
        contract.setPrice(request.getPrice());
        contract.setCurrency(request.getCurrency());
        contract.setSellerSigned(true);
        contract.setSellerSignedAt(LocalDateTime.now());
        contract.setStatus(ContractStatus.PENDING);

        contractRepository.save(contract);
        notificationService.sendNotificationToOneUser(order.getBuyer().getEmail(), "About your order", "Hey, look like your order's seller has sent the contract, you should check it out.");
    }

    public ContractResponse getContractDetail(Authentication authentication, Long contractId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));
        if (user.getRole() != Role.SELLER) {
            throw new BusinessException("User is not a seller", 400);
        }
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contract does not exist", 404));

        ContractResponse response = new ContractResponse();
        response.setContractId(contract.getId());
        response.setOrderId(contract.getOrder().getId());
        response.setContractCode(contract.getContractCode());
        response.setTitle(contract.getTitle());
        response.setContent(contract.getContent());
        response.setPrice(contract.getPrice());
        response.setCurrency(contract.getCurrency());
        response.setSellerSigned(contract.isSellerSigned());
        response.setSellerSignedAt(contract.getSellerSignedAt());
        response.setBuyerSigned(contract.isBuyerSigned());
        response.setBuyerSignedAt(contract.getBuyerSignedAt());
        response.setStatus(contract.getStatus());

        return response;
    }

    public List<ContractResponse> getAllContracts(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        List<Contract> list = contractRepository.findByOrder_Seller_Id(user.getId());
        return getSellerContracts(list);
    }

    private List<ContractResponse> getSellerContracts(List<Contract> contractList) {
        List<ContractResponse> responseList = new ArrayList<>();
        for (Contract contract : contractList) {
            ContractResponse response = new ContractResponse();
            response.setContractId(contract.getId());
            response.setOrderId(contract.getOrder().getId());
            response.setContractCode(contract.getContractCode());
            response.setTitle(contract.getTitle());
            response.setContent(contract.getContent());
            response.setPrice(contract.getPrice());
            response.setCurrency(contract.getCurrency());
            response.setSellerSigned(contract.isSellerSigned());
            response.setSellerSignedAt(contract.getSellerSignedAt());
            response.setBuyerSigned(contract.isBuyerSigned());
            response.setBuyerSignedAt(contract.getBuyerSignedAt());
            response.setStatus(contract.getStatus());
            responseList.add(response);
        }
        return responseList;
    }

}
