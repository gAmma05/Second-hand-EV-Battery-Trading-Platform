package com.example.SWP.controller.buyer;


import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.user.OrderResponse;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.service.buyer.BuyerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buyer/orders")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerOrderController {

    BuyerOrderService buyerOrderService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createOrder(
            Authentication authentication,
            @Valid CreateOrderRequest request
    ) {
        buyerOrderService.createOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Tạo đơn hàng thành công")
                        .build()
        );
    }

    @PatchMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            Authentication authentication,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        buyerOrderService.cancelOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Huỷ đơn hàng thành công")
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        OrderResponse response = buyerOrderService.getOrderDetail(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Lấy chi tiết đơn hàng thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        List<OrderResponse> response = buyerOrderService.getMyOrders(authentication);
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách đơn hàng thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            Authentication authentication,
            @RequestParam("status") OrderStatus status
    ) {
        List<OrderResponse> response = buyerOrderService.getOrdersByStatus(authentication, status);

        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách đơn hàng theo trạng thái thành công")
                        .data(response)
                        .build()
        );
    }
}
