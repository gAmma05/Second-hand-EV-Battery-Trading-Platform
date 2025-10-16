package com.example.SWP.controller.seller;

import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.service.seller.PriorityPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {
    PriorityPackageService priorityPackageService;

    @GetMapping("/priority-packages")
    public ResponseEntity<List<PriorityPackage>> getAllPriorityPackages() {
        List<PriorityPackage> packages = priorityPackageService.getAllPriorityPackages();
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
