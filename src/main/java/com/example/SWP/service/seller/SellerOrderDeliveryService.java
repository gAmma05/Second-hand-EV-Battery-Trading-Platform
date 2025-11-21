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
import com.example.SWP.service.escrow.EscrowService;
import com.example.SWP.service.ghn.GhnService;
import com.example.SWP.service.notification.NotificationService;
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
    OrderRepository orderRepository;
    NotificationService notificationService;
    EscrowService escrowService;

    /**
     * Tạo trạng thái giao hàng cho một đơn hàng.
     */
    public void createDeliveryStatus(Order order) {
        // Kiểm tra đơn hàng có tồn tại không
        if (order == null) {
            throw new BusinessException("Đơn hàng không tồn tại", 404);
        }

        // Kiểm tra xem đơn hàng đã có đơn hàng vận chuyển chưa
        OrderDelivery existing = orderDeliveryRepository.findByOrderId(order.getId());
        if (existing != null) {
            throw new BusinessException("Đơn hàng đã có trạng thái giao hàng", 400);
        }

        // Tạo đối tượng OrderDelivery mới
        OrderDelivery orderDelivery = new OrderDelivery();
        orderDelivery.setOrder(order);
        orderDelivery.setStatus(DeliveryStatus.PREPARING); // Trạng thái mặc định: đang chuẩn bị

        // Nếu phương thức giao hàng là GHN → tạo đơn trên GHN
        if (order.getDeliveryMethod() == DeliveryMethod.GHN) {
            ghnService.createGhnOrder(orderDelivery);
        }

        // Thiết lập ngày giao hàng dự kiến (mặc định cộng thêm 7 ngày)
        orderDelivery.setDeliveryDate(LocalDateTime.now().plusDays(7));

        // Thiết lập thời gian tạo và cập nhật
        orderDelivery.setCreatedAt(LocalDateTime.now());
        orderDelivery.setUpdatedAt(LocalDateTime.now());

        // Lưu vào DB
        orderDeliveryRepository.save(orderDelivery);
    }

    /**
     * Lấy danh sách tất cả đơn hàng giao thuộc về người bán hiện tại.
     */
    public List<OrderDeliveryResponse> getMyDeliveries(Authentication authentication) {
        // Xác thực và lấy thông tin người dùng hiện tại
        User user = validateService.validateCurrentUser(authentication);

        // Lấy tất cả đơn hàng giao thuộc về người bán
        List<OrderDelivery> deliveries = orderDeliveryRepository.findAllByOrder_Seller_Id(user.getId());

        // Chuyển đổi danh sách entity sang DTO để trả về cho client
        return orderDeliveryMapper.toOrderDeliveryResponseList(deliveries);
    }


    /**
     * Cập nhật trạng thái giao hàng thủ công cho một đơn hàng.
     */
    public OrderDeliveryResponse updateManualDeliveryStatus(Long id, DeliveryStatus newStatus) {
        // Lấy đơn hàng vận chuyển theo ID
        OrderDelivery orderDelivery = orderDeliveryRepository.findById(id).orElseThrow(
                () -> new BusinessException("Đơn hàng không tồn tại", 404)
        );

        // Không cho phép cập nhật thủ công nếu đơn hàng sử dụng GHN
        if (orderDelivery.getDeliveryProvider() == DeliveryProvider.GHN) {
            throw new BusinessException("Không thể cập nhật trạng thái thủ công cho đơn GHN", 400);
        }

        // Kiểm tra trạng thái giao hàng hiện tại
        DeliveryStatus currentStatus = orderDelivery.getStatus();

        // Người bán không thể cập nhật nếu đơn đã GIAO hoặc KHÁCH NHẬN
        if (currentStatus == DeliveryStatus.DELIVERED || currentStatus == DeliveryStatus.RECEIVED) {
            throw new BusinessException(
                    "Đơn hàng đã ở trạng thái " + currentStatus.getVietnameseName() + ", người bán không thể cập nhật thêm.",
                    400
            );
        }

        Order order = orderDelivery.getOrder();

        // Logic bỏ qua bước DELIVERING nếu đơn hàng là BUYER_PICKUP
        if (order.getDeliveryMethod() == DeliveryMethod.BUYER_PICKUP) {
            // Nếu trạng thái mới là PICKUP_PENDING, chỉ cho phép từ READY → PICKUP_PENDING
            if (newStatus == DeliveryStatus.PICKUP_PENDING) {
                if (currentStatus != DeliveryStatus.READY) {
                    throw new BusinessException(
                            "Chỉ có thể cập nhật trạng thái " + DeliveryStatus.PICKUP_PENDING.getVietnameseName() +
                                    " từ trạng thái " + DeliveryStatus.READY.getVietnameseName(),
                            400
                    );
                }
            } else {
                // Các trạng thái khác vẫn tuần tự
                if (newStatus.ordinal() != currentStatus.ordinal() + 1) {
                    DeliveryStatus[] allStatuses = DeliveryStatus.values();
                    String expectedStatusName = allStatuses[currentStatus.ordinal() + 1].getVietnameseName();

                    throw new BusinessException(
                            "Trạng thái hiện tại là " + currentStatus.getVietnameseName() +
                                    ", trạng thái tiếp theo phải là " + expectedStatusName,
                            400
                    );
                }
            }
        } else {
            // Đơn bình thường: tất cả vẫn tuần tự
            if (newStatus.ordinal() != currentStatus.ordinal() + 1) {
                DeliveryStatus[] allStatuses = DeliveryStatus.values();
                String expectedStatusName = allStatuses[currentStatus.ordinal() + 1].getVietnameseName();

                throw new BusinessException(
                        "Trạng thái hiện tại là " + currentStatus.getVietnameseName() +
                                ", trạng thái tiếp theo phải là " + expectedStatusName,
                        400
                );
            }
        }


        // Cập nhật trạng thái và thời gian cập nhật
        orderDelivery.setStatus(newStatus);
        orderDelivery.setUpdatedAt(LocalDateTime.now());

        // Nếu trạng thái mới là PICKUP_PENDING → kích hoạt hóa đơn chưa kích hoạt
        if (newStatus == DeliveryStatus.PICKUP_PENDING) {
            // Lấy hợp đồng liên quan
            Contract contract = contractRepository.findByOrder_Id(order.getId())
                    .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

            // Tìm hóa đơn chưa kích hoạt (INACTIVE) và kích hoạt
            invoiceRepository.findByContractAndStatus(contract, InvoiceStatus.INACTIVE)
                    .ifPresent(invoice -> {
                        invoice.setStatus(InvoiceStatus.ACTIVE);
                        invoice.setDueDate(LocalDateTime.now().plusDays(7));
                        invoiceRepository.save(invoice);
                    });
        }
        notificationService.sendNotificationToOneUser(orderDelivery.getOrder().getBuyer().getEmail(),
                "Về đơn hàng của bạn",
                "Đơn hàng của bạn đã được cập nhật trạng thái, hãy kiểm tra");

        // Lưu OrderDelivery sau khi cập nhật
        orderDeliveryRepository.save(orderDelivery);

        // Trả về DTO
        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }

    /**
     * Đồng bộ và cập nhật trạng thái giao hàng từ GHN.
     */
    public OrderDeliveryResponse updateGhnDeliveryStatus(Long orderDeliveryId) {
        // Lấy đơn hàng vận chuyển theo ID
        OrderDelivery orderDelivery = orderDeliveryRepository.findById(orderDeliveryId).orElseThrow(
                () -> new BusinessException("Đơn hàng không tồn tại", 404)
        );

        // Chỉ cho phép cập nhật GHN
        if (orderDelivery.getDeliveryProvider() != DeliveryProvider.GHN) {
            throw new BusinessException("Chỉ có thể cập nhật trạng thái đơn GHN", 400);
        }

        // Lấy trạng thái mới từ GHN
        DeliveryStatus ghnStatus = ghnService.getOrderStatus(orderDelivery.getDeliveryTrackingNumber());
        orderDelivery.setStatus(ghnStatus);
        orderDelivery.setUpdatedAt(LocalDateTime.now());
        orderDeliveryRepository.save(orderDelivery);

        // Nếu đã giao → cập nhật hóa đơn sang PAID
        if (ghnStatus == DeliveryStatus.DELIVERED) {
            Order order = orderDelivery.getOrder();

            Contract contract = contractRepository.findByOrder_Id(order.getId())
                    .orElseThrow(() -> new BusinessException("Hợp đồng không tồn tại", 404));

            invoiceRepository.findByContractAndStatus(contract, InvoiceStatus.INACTIVE)
                    .ifPresent(invoice -> {
                        invoice.setStatus(InvoiceStatus.PAID);
                        invoice.setDueDate(LocalDateTime.now().plusDays(7));
                        invoice.setPaidAt(LocalDateTime.now());
                        invoiceRepository.save(invoice);
                        escrowService.createEscrow(order.getSeller().getId(), order.getBuyer().getId(), order, false, invoice.getTotalPrice());
                    });


        }

        // Trả về DTO
        return orderDeliveryMapper.toOrderDeliveryResponse(orderDelivery);
    }

    /**
     * Lấy thông tin giao hàng của một đơn hàng
     */
    public OrderDeliveryResponse getDeliveryByOrderId(Authentication authentication, Long orderId) {
        // Xác thực người bán
        User user = validateService.validateCurrentUser(authentication);

        // Lấy đơn hàng theo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại", 404));

        // Kiểm tra quyền truy cập: người bán phải là chủ đơn
        if (!order.getSeller().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem thông tin giao hàng của đơn này", 403);
        }

        // Lấy thông tin giao hàng
        OrderDelivery delivery = orderDeliveryRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException("Đơn hàng này chưa có thông tin giao hàng", 400));

        // Chuyển sang DTO
        return orderDeliveryMapper.toOrderDeliveryResponse(delivery);
    }
}
