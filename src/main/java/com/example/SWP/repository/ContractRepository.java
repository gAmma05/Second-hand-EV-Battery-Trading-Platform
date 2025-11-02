package com.example.SWP.repository;

import com.example.SWP.entity.Contract;
import com.example.SWP.entity.Order;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByOrder_Seller(User user);
    Optional<Contract> findByOrder_Id(Long id);
    List<Contract> findByOrder_Buyer(User orderBuyer);
    boolean existsByOrderAndStatusIn(Order order, List<ContractStatus> pending);
}
