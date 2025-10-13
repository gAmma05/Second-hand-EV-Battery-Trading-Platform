package com.example.SWP.repository;

import com.example.SWP.entity.PackagePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackagePaymentRepository extends JpaRepository<PackagePayment, Long> {
    Optional<PackagePayment> findByOrderId(String orderId);
}
