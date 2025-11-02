package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.request.seller.RejectOrderRequest;
import com.example.SWP.dto.response.user.OrderResponse;
import com.example.SWP.entity.Order;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.service.seller.SellerOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SellerOrderController {

    SellerOrderService sellerOrderService;

    @GetMapping("/approve")
    public ResponseEntity<?> approveOrder(Authentication authentication, @RequestParam Long orderId) {
        sellerOrderService.approveOrder(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order approved successfully")
                        .build()
        );
    }

    @PostMapping("/reject")
    public ResponseEntity<?> rejectOrder(Authentication authentication, RejectOrderRequest rejectOrderRequest) {
        sellerOrderService.rejectOrder(authentication, rejectOrderRequest);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order rejected successfully")
                        .build()
        );
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderDetail(Authentication authentication, @PathVariable Long orderId) {
        OrderResponse response = sellerOrderService.getOrderDetail(authentication, orderId);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<?> getMyOrders(Authentication authentication) {

        List<OrderResponse> response = sellerOrderService.getMyOrders(authentication);

        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Order detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/status")
    public ResponseEntity<?> getOrdersByStatus(
            Authentication authentication,
            @RequestParam("status") OrderStatus status
    ) {
        List<OrderResponse> response = sellerOrderService.getOrdersByStatus(authentication, status);

        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Order detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

}
