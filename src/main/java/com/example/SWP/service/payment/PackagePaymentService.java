package com.example.SWP.service.payment;

import com.example.SWP.entity.PackagePayment;
import com.example.SWP.entity.User;
import com.example.SWP.entity.Package;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PackagePaymentRepository;
import com.example.SWP.repository.PackageRepository;
import com.example.SWP.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PackagePaymentService {

    PackagePaymentRepository packagePaymentRepository;
    PackageRepository packageRepository;
    UserRepository userRepository;
    MomoService momoService;

    /**
     * Create MoMo QR Payment for buying a package
     */
    @Transactional
    public String buyPackage(String email, Long packageId, PaymentMethod method) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        Package pack = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException("Package not found", 404));

        String orderId = "ORD-" + user.getId() + "-" + System.currentTimeMillis();

        PackagePayment payment = PackagePayment.builder()
                .orderId(orderId)
                .amount((long) pack.getPrice())
                .method(method)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .user(user)
                .packageBought(pack)
                .build();

        packagePaymentRepository.save(payment);

        try {
            // Tạo extraData JSON hợp lệ
            Map<String, Object> extraDataMap = new HashMap<>();
            extraDataMap.put("userId", user.getId());
            extraDataMap.put("packageId", pack.getId());
            String extraData = new ObjectMapper().writeValueAsString(extraDataMap);

            return momoService.createQrPayment(orderId, pack.getPrice(),
                    "Buy package " + pack.getPlanType(), extraData);

        } catch (BusinessException e) {
            throw new BusinessException("Failed to create MoMo QR payment: " + e.getMessage(), 502);
        } catch (Exception e) {
            throw new BusinessException("Internal error while creating MoMo QR payment", 500);
        }
    }


    /**
     * Handle MoMo callback when payment is completed
     */
    @Transactional
    public void handleMomoCallback(Map<String, Object> payload) {
        String orderId = (String) payload.get("orderId");
        String resultCode = String.valueOf(payload.get("resultCode"));
        String transactionId = String.valueOf(payload.get("transId"));

        PackagePayment packagePayment = packagePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Payment not found", 404));

        if ("0".equals(resultCode)) {
            // Successful packagePayment
            packagePayment.setStatus(PaymentStatus.SUCCESS);
            packagePayment.setTransactionId(transactionId);
            packagePayment.setUpdatedAt(LocalDateTime.now());
            packagePaymentRepository.save(packagePayment);

            // Update user’s plan
            User user = packagePayment.getUser();
            Package pack = packagePayment.getPackageBought();

            // Vì planType đã là enum SellerPlan
            user.setSellerPlan(pack.getPlanType());
            user.setRemainingPosts(pack.getPostLimit());
            user.setPlanExpiry(LocalDateTime.now().plusDays(pack.getDurationDays()));

            userRepository.save(user);
        } else {
            // Payment failed
            packagePayment.setStatus(PaymentStatus.FAILED);
            packagePaymentRepository.save(packagePayment);
        }
    }
}
