package com.example.SWP.controller.buyer;


import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.dto.response.ApiResponse;
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

    @PostMapping("/change-to-seller")
    public ResponseEntity<ApiResponse<String>> changeToSeller(Authentication authentication) {
        sellerService.changeToSeller(authentication);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Changed role to SELLER successfully")
                .build());
    }

    @GetMapping("/upgraded-to-seller")
    public ResponseEntity<ApiResponse<Boolean>> upgradedToSeller(Authentication authentication) {

        boolean upgraded = sellerService.upgradedToSeller(authentication);

        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .success(true)
                        .message(upgraded
                                ? "Người dùng đã năng cấp thành người bán"
                                : "Ngươi dùng chưa nâng cấp thành người bán")
                        .data(upgraded)
                        .build()
        );
    }
}
