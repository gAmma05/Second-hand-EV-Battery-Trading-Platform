package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.seller.SellerOrderResponse;
import com.example.SWP.dto.response.seller.RejectOrderResponse;
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
    public ResponseEntity<ApiResponse<Void>> approveOrder(Authentication authentication, @RequestParam Long orderId) {
        sellerOrderService.approveOrder(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order approved successfully")
                        .build()
        );
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<Void>> rejectOrder(Authentication authentication, RejectOrderResponse rejectOrderResponse) {
        sellerOrderService.rejectOrder(authentication, rejectOrderResponse);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order rejected successfully")
                        .build()
        );
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> getOrderDetail(Authentication authentication, @RequestParam Long orderId) {
        SellerOrderResponse response = sellerOrderService.getOrderDetail(authentication, orderId);
        if(response == null){
            return ResponseEntity.ok(
                    ApiResponse.<SellerOrderResponse>builder()
                            .success(false)
                            .message("Order detail not found")
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<SellerOrderResponse>builder()
                        .success(true)
                        .message("Order detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SellerOrderResponse>>> getMyOrders(Authentication authentication) {
        List<SellerOrderResponse> orderList = sellerOrderService.getAllOrders(authentication);
        if(orderList == null || orderList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<SellerOrderResponse>>builder()
                            .success(true)
                            .message("No orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<SellerOrderResponse>>builder()
                        .success(true)
                        .message("All orders fetched successfully")
                        .data(orderList)
                        .build()
        );
    }

    @GetMapping("/pending-list")
    public ResponseEntity<ApiResponse<List<SellerOrderResponse>>> getPendingOrders(Authentication authentication) {
        List<SellerOrderResponse> pendingList = sellerOrderService.getPendingOrder(authentication);
        if(pendingList == null || pendingList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<SellerOrderResponse>>builder()
                            .success(true)
                            .message("No pending orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<SellerOrderResponse>>builder()
                        .success(true)
                        .message("Pending orders fetched successfully")
                        .data(pendingList)
                        .build()
        );
    }

    @GetMapping("/approved-list")
    public ResponseEntity<ApiResponse<List<SellerOrderResponse>>> getApprovedOrders(Authentication authentication) {
        List<SellerOrderResponse> approvedList = sellerOrderService.getApprovedOrder(authentication);
        if(approvedList == null || approvedList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<SellerOrderResponse>>builder()
                            .success(true)
                            .message("No approved orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<SellerOrderResponse>>builder()
                        .success(true)
                        .message("Approved orders fetched successfully")
                        .data(approvedList)
                        .build()
        );
    }

    @GetMapping("/rejected-list")
    public ResponseEntity<ApiResponse<List<SellerOrderResponse>>> getRejectedOrders(Authentication authentication){
        List<SellerOrderResponse> rejectedList = sellerOrderService.getRejectedOrder(authentication);
        if(rejectedList == null || rejectedList.isEmpty()){
            return ResponseEntity.ok(
                    ApiResponse.<List<SellerOrderResponse>>builder()
                            .success(true)
                            .message("No rejected orders")
                            .data(new ArrayList<>())
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<SellerOrderResponse>>builder()
                        .success(true)
                        .message("Rejected orders fetched successfully")
                        .data(rejectedList)
                        .build()
        );
    }


}
