package com.example.SWP.repository;

import com.example.SWP.entity.Contract;
import com.example.SWP.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByOrder_Buyer_Id(Long buyerId);
    List<Contract> findByOrder_Seller_Id(Long sellerId);
    List<Contract> findByOrder_Buyer_IdAndStatus(Long buyerId, ContractStatus status);
    List<Contract> findByOrder_Seller_IdAndStatus(Long sellerId, ContractStatus status);
}
