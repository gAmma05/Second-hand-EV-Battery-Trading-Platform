package com.example.SWP.repository;

import com.example.SWP.entity.SellerPackagePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackagePaymentRepository extends JpaRepository<SellerPackagePayment, Long> {
    Optional<SellerPackagePayment> findByOrderId(String orderId);
}
