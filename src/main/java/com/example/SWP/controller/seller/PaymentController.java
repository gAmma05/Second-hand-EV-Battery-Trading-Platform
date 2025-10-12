package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.Package;
import com.example.SWP.entity.PackagePayment;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PackagePaymentRepository;
import com.example.SWP.repository.PackageRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.payment.PackagePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.SWP.repository.PackagePaymentRepository;


import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/seller/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PackagePaymentService packagePaymentService;

    /**
     * API tạo QR code để thanh toán gói
     */
    @PostMapping("/packages/{packageId}/buy")
    public ResponseEntity<ApiResponse<String>> buyPackage(
            @PathVariable Long packageId,
            @RequestParam PaymentMethod method,
            Authentication authentication
    ) {
        String email = authentication.getName();

        String qrCode = packagePaymentService.buyPackage(email, packageId, method);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("QR code created")
                        .data(qrCode)
                        .build()
        );
    }

    /**
     * Callback từ MoMo sau khi thanh toán
     */
    @PostMapping("/notify")
    public ResponseEntity<String> momoNotify(@RequestBody Map<String, Object> payload) {
        System.out.println("Received payload: " + payload);
        packagePaymentService.handleMomoCallback(payload);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/return")
    public ResponseEntity<Void> momoReturn(@RequestParam Map<String, String> params) {
        String resultCode = params.get("resultCode");
        String url;

        if ("0".equals(resultCode)) {
            url = "http://localhost:3000/payment-success";
        } else {
            String message = params.get("message");
            url = "http://localhost:3000/payment-failed?message=" + message;
        }

        return ResponseEntity.status(302)
                .header("Location", url)
                .build();
    }


}
