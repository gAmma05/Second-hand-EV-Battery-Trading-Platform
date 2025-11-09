package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/order-deliveries")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SellerOrderDeliveryController {
    SellerOrderDeliveryService sellerOrderDeliveryService;

    /**
     * Lấy danh sách tất cả đơn hàng giao của người bán hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDeliveryResponse>>> getMyDeliveries(
            Authentication authentication
    ) {
        List<OrderDeliveryResponse> responses = sellerOrderDeliveryService.getMyDeliveries(authentication);
        return ResponseEntity.ok(ApiResponse.<List<OrderDeliveryResponse>>builder()
                .success(true)
                .message("Lấy danh sách đơn hàng giao của bạn thành công")
                .data(responses)
                .build());
    }

    /**
     * Lấy chi tiết đơn hàng giao theo ID đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDeliveryResponse>> getDeliveryByOrderId(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        OrderDeliveryResponse response = sellerOrderDeliveryService.getDeliveryByOrderId(authentication, orderId);
        return ResponseEntity.ok(ApiResponse.<OrderDeliveryResponse>builder()
                .success(true)
                .message("Lấy chi tiết đơn hàng giao thành công")
                .data(response)
                .build());
    }

    /**
     * Cập nhật trạng thái giao hàng thủ công bởi người bán
     */
    @PutMapping("/{orderDeliveryId}/manual")
    public ResponseEntity<ApiResponse<OrderDeliveryResponse>> updateManualStatus(
            @PathVariable Long orderDeliveryId,
            @RequestParam DeliveryStatus deliveryStatus
    ) {
        OrderDeliveryResponse response = sellerOrderDeliveryService.updateManualDeliveryStatus(orderDeliveryId, deliveryStatus);
        return ResponseEntity.ok(
                ApiResponse.<OrderDeliveryResponse>builder()
                        .success(true)
                        .message("Cập nhật trạng thái giao hàng thủ công thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Đồng bộ trạng thái giao hàng từ GHN
     */
    @PutMapping("/{orderDeliveryId}/ghn")
    public ResponseEntity<ApiResponse<OrderDeliveryResponse>> updateGhnStatus(
            @PathVariable Long orderDeliveryId
    ) {
        OrderDeliveryResponse response = sellerOrderDeliveryService.updateGhnDeliveryStatus(orderDeliveryId);
        return ResponseEntity.ok(
                ApiResponse.<OrderDeliveryResponse>builder()
                        .success(true)
                        .message("Đồng bộ trạng thái giao hàng GHN thành công")
                        .data(response)
                        .build()
        );
    }
}
