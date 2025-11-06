package com.example.SWP.controller.buyer;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.service.buyer.BuyerOrderDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buyer/order-deliveries")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BuyerOrderDeliveryController {

    BuyerOrderDeliveryService buyerOrderDeliveryService;

    @PostMapping("/{orderDeliveryId}/confirm-received")
    public ResponseEntity<ApiResponse<Void>> confirmReceived(
            Authentication authentication,
            @PathVariable Long orderDeliveryId
    ) {
        buyerOrderDeliveryService.confirmReceived(authentication, orderDeliveryId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Xác nhận nhận hàng thành công")
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDeliveryResponse>>> getMyDeliveries(Authentication authentication) {
        List<OrderDeliveryResponse> responses = buyerOrderDeliveryService.getMyDeliveries(authentication);
        return ResponseEntity.ok(
                ApiResponse.<List<OrderDeliveryResponse>>builder()
                        .success(true)
                        .message("Danh sách đơn hàng giao của bạn")
                        .data(responses)
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDeliveryResponse>> getDeliveryByOrderId(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        OrderDeliveryResponse response = buyerOrderDeliveryService.getDeliveryByOrderId(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<OrderDeliveryResponse>builder()
                        .success(true)
                        .message("Chi tiết đơn hàng giao")
                        .data(response)
                        .build()
        );
    }


}
