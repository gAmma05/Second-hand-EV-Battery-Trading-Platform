package com.example.SWP.repository;

import com.example.SWP.entity.Invoice;
import com.example.SWP.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> getInvoiceByContract_IdAndStatus(Long contractId, InvoiceStatus status);
    List<Invoice> getInvoiceByContract_Order_Buyer_IdAndStatus(Long buyerId, InvoiceStatus status);
    Invoice getInvoiceByIdAndContract_Order_Buyer_Id(Long invoiceId, Long buyerId);
}
