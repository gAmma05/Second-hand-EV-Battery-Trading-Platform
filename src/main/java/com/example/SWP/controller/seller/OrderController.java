package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.seller.OrderResponse;
import com.example.SWP.dto.response.seller.RejectOrderResponse;
import com.example.SWP.service.seller.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<Void>> approveOrder(Authentication authentication, Long orderId) {
        orderService.approveOrder(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order approved successfully")
                        .build()
        );
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<Void>> rejectOrder(Authentication authentication, RejectOrderResponse rejectOrderResponse) {
        orderService.rejectOrder(authentication, rejectOrderResponse);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order rejected successfully")
                        .build()
        );
    }

    @PostMapping("/order-detail")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(Authentication authentication, Long orderId) {
        OrderResponse response = orderService.getOrderDetail(authentication, orderId);
        if(response == null){
            return ResponseEntity.ok(
                    ApiResponse.<OrderResponse>builder()
                            .success(false)
                            .message("Order detail not found")
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        List<OrderResponse> orderList = orderService.getAllOrders(authentication);
        if(orderList == null || orderList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<OrderResponse>>builder()
                            .success(true)
                            .message("No orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("All orders fetched successfully")
                        .data(orderList)
                        .build()
        );
    }

    @GetMapping("/pending-list")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getPendingOrders(Authentication authentication) {
        List<OrderResponse> pendingList = orderService.getPendingOrder(authentication);
        if(pendingList == null || pendingList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<OrderResponse>>builder()
                            .success(true)
                            .message("No pending orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Pending orders fetched successfully")
                        .data(pendingList)
                        .build()
        );
    }

    @GetMapping("/approved-list")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getApprovedOrders(Authentication authentication) {
        List<OrderResponse> approvedList = orderService.getApprovedOrder(authentication);
        if(approvedList == null || approvedList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<OrderResponse>>builder()
                            .success(true)
                            .message("No approved orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Approved orders fetched successfully")
                        .data(approvedList)
                        .build()
        );
    }

    @GetMapping("/rejected-list")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRejectedOrders(Authentication authentication){
        List<OrderResponse> rejectedList = orderService.getRejectedOrder(authentication);
        if(rejectedList == null || rejectedList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<OrderResponse>>builder()
                            .success(true)
                            .message("No rejected orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Rejected orders fetched successfully")
                        .data(rejectedList)
                        .build()
        );
    }
}
