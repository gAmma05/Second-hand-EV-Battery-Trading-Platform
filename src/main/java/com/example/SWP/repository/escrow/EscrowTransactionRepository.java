package com.example.SWP.repository.escrow;

import com.example.SWP.entity.escrow.EscrowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, Long> {

}
