package com.example.SWP.controller.buyer;


import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.user.OrderResponse;
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

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(Authentication authentication, @PathVariable Long orderId) {
        OrderResponse response = buyerOrderService.getOrderDetail(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping()
    public ResponseEntity<?> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<OrderResponse> response = buyerOrderService.getMyOrders(authentication, page, size);
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Order detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createOrder(Authentication authentication, CreateOrderRequest request) {
        buyerOrderService.createOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order created successfully")
                        .build()
        );
    }

    @PatchMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(Authentication authentication, @Valid @RequestBody CancelOrderRequest request) {
        buyerOrderService.cancelOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .build()
        );
    }
}
