package com.example.SWP.controller.buyer;


import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.buyer.BuyerOrderResponse;
import com.example.SWP.service.buyer.BuyerOrderDeliveryService;
import com.example.SWP.service.buyer.BuyerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buyer/orders")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerOrderController {

    BuyerOrderService buyerOrderService;

    @GetMapping("/detail")
    public ResponseEntity<?> getOrderDetail(@RequestParam Long orderId) {
        BuyerOrderResponse response = buyerOrderService.getOrderDetail(orderId);
        return ResponseEntity.ok(
                ApiResponse.<BuyerOrderResponse>builder()
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

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(Authentication authentication, CancelOrderRequest request) {
        buyerOrderService.cancelOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .build()
        );
    }
}
