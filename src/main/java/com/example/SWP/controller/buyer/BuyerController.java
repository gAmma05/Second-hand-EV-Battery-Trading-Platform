package com.example.SWP.controller.buyer;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.service.seller.SellerService;
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

    @PostMapping("/upgrade")
    public ResponseEntity<ApiResponse<String>> upgradeToSeller(
            Authentication authentication,
            @RequestBody UpgradeToSellerRequest request
    ) {
        sellerService.upgradeToSeller(authentication, request);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Upgraded to seller successfully")
                .build());
    }

}
