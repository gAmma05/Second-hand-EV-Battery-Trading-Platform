package com.example.SWP.repository;

import com.example.SWP.entity.PriorityPackagePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PriorityPackagePaymentRepository extends JpaRepository<PriorityPackagePayment, Long> {
}
