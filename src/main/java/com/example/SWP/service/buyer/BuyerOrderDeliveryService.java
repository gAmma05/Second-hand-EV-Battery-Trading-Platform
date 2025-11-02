package com.example.SWP.service.buyer;

import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.enums.InvoiceStatus;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.OrderDeliveryMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.InvoiceRepository;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.OrderRepository;
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
public class BuyerOrderDeliveryService {

    ValidateService validateService;
    OrderDeliveryRepository orderDeliveryRepository;
    InvoiceRepository invoiceRepository;
    ContractRepository contractRepository;
    OrderDeliveryMapper orderDeliveryMapper;

    public void confirmReceived(Authentication authentication, Long orderDeliveryId) {
        User user = validateService.validateCurrentUser(authentication);

        OrderDelivery orderDelivery = orderDeliveryRepository.findById(orderDeliveryId).orElseThrow(
                () -> new BusinessException("Đơn hàng vận chuyển không tồn tại", 404)
        );

        Order order = orderDelivery.getOrder();

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xác nhận đơn hàng này", 403);
        }

        if (orderDelivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new BusinessException("Đơn hàng chưa được giao", 400);
        }

        orderDelivery.setStatus(DeliveryStatus.RECEIVED);
        orderDelivery.setUpdatedAt(LocalDateTime.now());
        orderDeliveryRepository.save(orderDelivery);

        if(order.getPaymentType() == PaymentType.DEPOSIT) {
            Contract contract = contractRepository.findByOrder_Id(order.getId())
                    .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

            invoiceRepository.findByContractIdAndStatus(contract.getId(), InvoiceStatus.INACTIVE)
                    .ifPresent(invoice -> {
                        invoice.setStatus(InvoiceStatus.ACTIVE);
                        invoice.setDueDate(LocalDateTime.now().plusDays(7));
                        invoiceRepository.save(invoice);
                    });
        }
    }

    public OrderDeliveryResponse getDeliveryDetail(Authentication authentication, Long orderDeliveryId) {
        User user = validateService.validateCurrentUser(authentication);

        OrderDelivery orderDelivery = orderDeliveryRepository.findById(orderDeliveryId)
                .orElseThrow(() -> new BusinessException("Trạng thái giao hàng không tồn tại", 404));

        Order order = orderDelivery.getOrder();

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem thông tin giao hàng này", 403);
        }

        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }


    public List<OrderDeliveryResponse> getMyDeliveries(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<OrderDelivery> deliveries = orderDeliveryRepository.findAllByOrder_Buyer_Id(user.getId());

        return orderDeliveryMapper.toOrderDeliveryResponseList(deliveries);
    }
}
