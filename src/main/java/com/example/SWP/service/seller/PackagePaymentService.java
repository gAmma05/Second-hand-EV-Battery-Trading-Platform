package com.example.SWP.service.seller;

import com.example.SWP.configuration.VnPayConfig;
import com.example.SWP.entity.Package;
import com.example.SWP.entity.PackagePayment;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PackagePaymentRepository;
import com.example.SWP.repository.PackageRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PackagePaymentService {

    PackagePaymentRepository packagePaymentRepository;
    UserRepository userRepository;
    PackageRepository packageRepository;
    VnPayConfig vnp_Config;

    public String createPackagePaymentOrder(String email, Long packageId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException("Package not found", 404));

        PackagePayment payment = PackagePayment.builder()
                .user(user)
                .boughtPackage(pkg)
                .amount(pkg.getPrice())
                .status(PaymentStatus.PENDING)
                .orderId(String.valueOf(System.currentTimeMillis()))
                .createdAt(LocalDateTime.now())
                .build();
        packagePaymentRepository.save(payment);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_Config.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(pkg.getPrice() * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", payment.getOrderId());
        vnp_Params.put("vnp_OrderInfo", "Purchase package " + pkg.getPlanType());
        vnp_Params.put("vnp_OrderType", "billpayment");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_Config.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", new SimpleDateFormat("yyyyMMddHHmmss").format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnp_Config.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return vnp_Config.getPayUrl() + "?" + query;
    }


    public PackagePayment updatePackagePayment(String orderId, String responseCode) {
        PackagePayment packagePayment = packagePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("PackagePayment not found", 404));

        //Thanh toan thanh cong
        if ("00".equals(responseCode)) {
            packagePayment.setStatus(PaymentStatus.SUCCESS);

            User user = packagePayment.getUser();
            Package pkg = packagePayment.getBoughtPackage();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiry = user.getPlanExpiry();

            //Goi van con han
            boolean isStillActive = expiry != null && expiry.isAfter(now);

            LocalDateTime newExpiry;
            if (isStillActive) {
                //Neu goi van con han thi van giu so luot dang cu
                newExpiry = expiry.plusDays(pkg.getDurationDays());

                int oldRemaining = user.getRemainingPosts();
                int newTotalPosts = oldRemaining + pkg.getPostLimit();
                user.setRemainingPosts(newTotalPosts);
            } else {
                //Neu het han thi xoa so luot dang cu
                newExpiry = now.plusDays(pkg.getDurationDays());
                user.setRemainingPosts(pkg.getPostLimit());
            }

            user.setSellerPlan(pkg.getPlanType());
            user.setPlanExpiry(newExpiry);

            userRepository.save(user);

        } else {
            packagePayment.setStatus(PaymentStatus.FAILED);
        }

        packagePayment.setVnpResponseCode(responseCode);
        packagePayment.setUpdatedAt(LocalDateTime.now());
        return packagePaymentRepository.save(packagePayment);
    }

}
