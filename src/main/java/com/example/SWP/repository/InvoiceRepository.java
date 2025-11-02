package com.example.SWP.repository;

import com.example.SWP.entity.Invoice;
import com.example.SWP.entity.Order;
import com.example.SWP.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> getInvoiceByContract_Order_Buyer_IdAndStatus(Long buyerId, InvoiceStatus status);
    List<Invoice> getInvoiceByContract_Order_Buyer_Id(Long buyerId);
    Optional<Invoice> getInvoiceByIdAndContract_Order_Buyer_Id(Long invoiceId, Long buyerId);
    Optional<Invoice> findByContractIdAndStatus(Long contractId, InvoiceStatus status);
    List<Invoice> getInvoiceByContract_Order_IdAndContract_Order_Buyer_Id(Long orderId, Long id);
    boolean existsByContract_OrderAndStatus(Order order, InvoiceStatus invoiceStatus);
}
