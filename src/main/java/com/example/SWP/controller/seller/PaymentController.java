package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.SellerPackagePayment;
import com.example.SWP.entity.PriorityPackagePayment;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.service.seller.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/seller/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService packagePaymentService;

    @GetMapping("/seller-package")
    public ResponseEntity<ApiResponse<String>> sellerPackagePayment(
            @RequestParam Long packageId,
            Authentication authentication) {

        // Thực hiện thanh toán từ ví
        packagePaymentService.sellerPackagePayment(authentication.getName(), packageId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Payment successful via wallet")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
