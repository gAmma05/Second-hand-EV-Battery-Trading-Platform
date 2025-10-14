package com.example.SWP.service.seller;

import com.example.SWP.entity.*;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.*;
import com.example.SWP.service.payment.VnPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PackagePaymentRepository packagePaymentRepository;
    UserRepository userRepository;
    PackageRepository packageRepository;
    VnPayService vnPayService;
    PostRepository postRepository;
    PriorityPackageRepository priorityPackageRepository;
    PriorityPackagePaymentRepository priorityPackagePaymentRepository;

    @NonFinal
    @Value("${vnpay.returnUrl.sellerPackage}")
    String sellerPackageReturnUrl;

    @NonFinal
    @Value("${vnpay.returnUrl.priorityPackage}")
    String priorityPackageReturnUrl;

    public String sellerPackagePayment(String email, Long packageId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        SellerPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new BusinessException("Package not found", 404));

        SellerPackagePayment payment = SellerPackagePayment.builder()
                .user(user)
                .boughtPackage(pkg)
                .amount(pkg.getPrice())
                .status(PaymentStatus.PENDING)
                .orderId(String.valueOf(System.currentTimeMillis()))
                .createdAt(LocalDateTime.now())
                .build();
        packagePaymentRepository.save(payment);

        String description = "Purchase seller package " + pkg.getType();
        return vnPayService.createPaymentUrl(payment.getOrderId(), payment.getAmount(), description, sellerPackageReturnUrl);
    }


    public SellerPackagePayment sellerPackagePaymentReturn(String orderId, String responseCode) {
        SellerPackagePayment packagePayment = packagePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("PackagePayment not found", 404));

        //Thanh toan thanh cong
        if ("00".equals(responseCode)) {
            packagePayment.setStatus(PaymentStatus.SUCCESS);

            User user = packagePayment.getUser();
            SellerPackage pkg = packagePayment.getBoughtPackage();

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

            user.setSellerPlan(pkg.getType());
            user.setPlanExpiry(newExpiry);

            userRepository.save(user);

        } else {
            packagePayment.setStatus(PaymentStatus.FAILED);
        }

        packagePayment.setVnpResponseCode(responseCode);
        packagePayment.setUpdatedAt(LocalDateTime.now());
        return packagePaymentRepository.save(packagePayment);
    }

    public String priorityPackagePayment(String email, Long postId, Long priorityPackageId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", 404));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        PriorityPackage priorityPackage = priorityPackageRepository.findById(priorityPackageId)
                .orElseThrow(() -> new BusinessException("Priority package not found", 404));

        PriorityPackagePayment payment = PriorityPackagePayment.builder()
                .post(post)
                .priorityPackage(priorityPackage)
                .amount(priorityPackage.getPrice())
                .status(PaymentStatus.PENDING)
                .orderId(String.valueOf(System.currentTimeMillis()))
                .createdAt(LocalDateTime.now())
                .build();

        priorityPackagePaymentRepository.save(payment);

        String description = "Purchase priority package: " + priorityPackage.getType();
        return vnPayService.createPaymentUrl(payment.getOrderId(), payment.getAmount(), description, priorityPackageReturnUrl);
    }


    public PriorityPackagePayment priorityPackagePaymentReturn(String orderId, String responseCode) {
        PriorityPackagePayment payment = priorityPackagePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("PostPriorityPayment not found", 404));

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            Post post = payment.getPost();
            post.setPriority(true);
            post.setExpiryDate(LocalDateTime.now().plusDays(payment.getPriorityPackage().getDurationDays()));

            postRepository.save(post);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        payment.setVnpResponseCode(responseCode);
        payment.setUpdatedAt(LocalDateTime.now());
        return priorityPackagePaymentRepository.save(payment);
    }

}
