package com.example.SWP.controller.buyer;


import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.service.buyer.BuyerService;
import com.example.SWP.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buyer/order")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerOrderController {

    SellerService sellerService;

    BuyerService buyerService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createOrder(Authentication authentication, CreateOrderRequest request) {
        buyerService.createOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order created successfully")
                        .build()
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(Authentication authentication, CancelOrderRequest request) {
        buyerService.cancelOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .build()
        );
    }
}
