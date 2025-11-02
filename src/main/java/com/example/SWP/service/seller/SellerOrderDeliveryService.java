package com.example.SWP.service.seller;

import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.User;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.OrderDeliveryMapper;
import com.example.SWP.repository.ContractRepository;
import com.example.SWP.repository.InvoiceRepository;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.service.ghn.GhnService;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerOrderDeliveryService {

    OrderDeliveryRepository orderDeliveryRepository;
    GhnService ghnService;
    OrderDeliveryMapper orderDeliveryMapper;
    ValidateService validateService;
    InvoiceRepository invoiceRepository;
    ContractRepository contractRepository;

    public void createDeliveryStatus(Order order) {
        if (order == null) {
            throw new BusinessException("Order không tồn tại", 404);
        }

        OrderDelivery existing = orderDeliveryRepository.findByOrderId(order.getId());
        if (existing != null) {
            throw new BusinessException("Order đã có trạng thái giao hàng", 400);
        }

        OrderDelivery orderDelivery = new OrderDelivery();

        orderDelivery.setOrder(order);
        orderDelivery.setStatus(DeliveryStatus.PREPARING);

        if (order.getDeliveryMethod() == DeliveryMethod.GHN) {
            ghnService.createGhnOrder(orderDelivery);
        }

        orderDelivery.setDeliveryDate(LocalDateTime.now().plusDays(7));
        orderDelivery.setCreatedAt(LocalDateTime.now());
        orderDelivery.setUpdatedAt(LocalDateTime.now());

        orderDeliveryRepository.save(orderDelivery);
    }

    public OrderDeliveryResponse getDeliveryDetail(Authentication authentication, Long orderDeliveryId) {
        User user = validateService.validateCurrentUser(authentication);

        OrderDelivery delivery = orderDeliveryRepository.findById(orderDeliveryId)
                .orElseThrow(() -> new BusinessException("Thông tin giao hàng không tồn tại", 404));

        Order order = delivery.getOrder();

        if (!order.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem đơn hàng này", 403);
        }

        return orderDeliveryMapper.toOrderDeliveryResponse(delivery);
    }


    public List<OrderDeliveryResponse> getMyDeliveries(Authentication authentication) {
        User user = validateService.validateCurrentUser(authentication);

        List<OrderDelivery> deliveries = orderDeliveryRepository.findAllByOrder_Seller_Id(user.getId());

        return orderDeliveryMapper.toOrderDeliveryResponseList(deliveries);
    }

    public OrderDeliveryResponse updateManualDeliveryStatus(Long id, DeliveryStatus newStatus) {
        OrderDelivery orderDelivery = orderDeliveryRepository.findById(id).orElseThrow(
                () -> new BusinessException("Đơn hàng không tồn tại", 404)
        );

        if (orderDelivery.getDeliveryProvider() == DeliveryProvider.GHN) {
            throw new BusinessException("Không thể cập nhật trạng thái thủ công cho đơn GHN", 400);
        }

        if (orderDelivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new BusinessException("Không thể cập nhật trạng thái cho đơn đã giao", 400);
        }

        orderDelivery.setStatus(newStatus);
        orderDelivery.setUpdatedAt(LocalDateTime.now());

        if (newStatus == DeliveryStatus.DELIVERED) {
            Order order = orderDelivery.getOrder();

            if (order.getPaymentType() == PaymentType.DEPOSIT) {
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

        orderDeliveryRepository.save(orderDelivery);

        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }


    public OrderDeliveryResponse updateGhnDeliveryStatus(Long orderDeliveryId) {
        OrderDelivery orderDelivery = orderDeliveryRepository.findById(orderDeliveryId).orElseThrow(
                () -> new BusinessException("Đơn hàng không tồn tại", 404)
        );

        if (orderDelivery.getDeliveryProvider() != DeliveryProvider.GHN) {
            throw new BusinessException("Chỉ có thể cập nhật trạng thái đơn GHN", 400);
        }

        DeliveryStatus ghnStatus = ghnService.getOrderStatus(orderDelivery.getDeliveryTrackingNumber());
        orderDelivery.setStatus(ghnStatus);

        orderDelivery.setUpdatedAt(LocalDateTime.now());
        orderDeliveryRepository.save(orderDelivery);

        if (ghnStatus == DeliveryStatus.DELIVERED) {
            Order order = orderDelivery.getOrder();

            if (order.getPaymentType() == PaymentType.DEPOSIT) {
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

        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }

}
