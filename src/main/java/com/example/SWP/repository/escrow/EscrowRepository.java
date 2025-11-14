package com.example.SWP.repository.escrow;

import com.example.SWP.entity.escrow.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    Optional<Escrow> findByOrder_Id(Long orderId);
}
