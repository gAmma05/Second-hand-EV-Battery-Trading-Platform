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
    public ResponseEntity<?> sellerPackagePayment(
            @RequestParam Long packageId,
            Authentication authentication) {

        String paymentUrl = packagePaymentService.sellerPackagePayment(authentication.getName(), packageId);

        Map<String, Object> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller-package/return")
    public ResponseEntity<ApiResponse<Void>> sellerPackageReturn(@RequestParam Map<String, String> params) {
        try {
            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");

            SellerPackagePayment payment = packagePaymentService.sellerPackagePaymentReturn(orderId, responseCode);

            boolean isSuccess = payment.getStatus() == PaymentStatus.SUCCESS;

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(isSuccess)
                    .message(isSuccess ? "Payment successful" : "Payment failed")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Payment error: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/priority-package/return")
    public ResponseEntity<ApiResponse<Void>> priorityPackageReturn(@RequestParam Map<String, String> params) {
        try {
            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");

            PriorityPackagePayment payment = packagePaymentService.priorityPackagePaymentReturn(orderId, responseCode);

            boolean isSuccess = payment.getStatus() == PaymentStatus.SUCCESS;

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(isSuccess)
                    .message(isSuccess ? "Payment successful" : "Payment failed")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Payment error: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
