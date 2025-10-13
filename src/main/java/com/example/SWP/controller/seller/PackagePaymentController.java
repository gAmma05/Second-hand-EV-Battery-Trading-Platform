package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.Package;
import com.example.SWP.entity.PackagePayment;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PackageRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.seller.PackagePaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/seller/package-payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PackagePaymentController {

    PackagePaymentService packagePaymentService;

    @GetMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestParam Long packageId,
            Authentication authentication) {

        String paymentUrl = packagePaymentService.createPackagePaymentOrder(authentication.getName(), packageId);

        Map<String, Object> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<Void>> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");

            PackagePayment payment = packagePaymentService.updatePackagePayment(orderId, responseCode);

            String message = (payment.getStatus() == PaymentStatus.SUCCESS)
                    ? "Payment successful"
                    : "Payment failed";

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message(message)
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
