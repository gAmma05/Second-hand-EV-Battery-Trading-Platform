package com.example.SWP.repository;

import com.example.SWP.entity.SellerPackage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<SellerPackage, Long> {
}
