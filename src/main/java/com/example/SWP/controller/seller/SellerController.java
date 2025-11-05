package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.service.buyer.BuyerService;
import com.example.SWP.service.seller.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SellerController {
    SellerService sellerService;
    BuyerService buyerService;

    @PostMapping("/change-to-buyer")
    public ResponseEntity<ApiResponse<String>> changeToBuyer(Authentication authentication) {
        buyerService.changeToBuyer(authentication);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Chuyển đổi thành công sang người mua")
                .build());
    }

    @GetMapping("/priority-packages")
    public ResponseEntity<List<PriorityPackage>> getAllPriorityPackages() {
        List<PriorityPackage> packages = sellerService.getAllPriorityPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/seller-packages")
    public ResponseEntity<List<SellerPackage>> getAllSellerPackages() {
        List<SellerPackage> packages = sellerService.getAllSellerPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/delivery-methods")
    public ResponseEntity<List<DeliveryMethod>> getAllDeliveryMethods() {
        List<DeliveryMethod> methods = Arrays.asList(DeliveryMethod.values());
        return ResponseEntity.ok(methods);
    }

    @GetMapping("/payment-types")
    public ResponseEntity<List<PaymentType>> getAllPaymentTypes() {
        List<PaymentType> types = Arrays.asList(PaymentType.values());
        return ResponseEntity.ok(types);
    }
}
