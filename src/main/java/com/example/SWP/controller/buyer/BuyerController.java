package com.example.SWP.controller.buyer;


import com.example.SWP.dto.request.buyer.CancelOrderRequest;
import com.example.SWP.dto.request.buyer.CreateOrderRequest;
import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.service.buyer.BuyerService;
import com.example.SWP.service.seller.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buyer")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerController {

    SellerService sellerService;

    BuyerService buyerService;

    @PostMapping("/order/create-order")
    public ResponseEntity<ApiResponse<Void>> createOrder(Authentication authentication, CreateOrderRequest request) {
        buyerService.createOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order created successfully")
                        .build()
        );
    }

    @PostMapping("/order/cancel-order")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(Authentication authentication, CancelOrderRequest request){
        buyerService.cancelOrder(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .build()
        );
    }

    @PostMapping("/upgrade-to-seller")
    public ResponseEntity<ApiResponse<String>> upgradeToSeller(
            Authentication authentication,
            @Valid @RequestBody UpgradeToSellerRequest request
    ) {
        sellerService.upgradeToSeller(authentication, request);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Upgraded to seller successfully")
                .build());
    }

}
