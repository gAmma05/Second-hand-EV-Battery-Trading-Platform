package com.example.SWP.repository;

import com.example.SWP.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByOrder_Buyer_Id(Long buyerId);
    List<Complaint> findByOrder_Seller_Id(Long sellerId);
    Complaint findByIdAndOrder_Buyer_Id(Long complaintId, Long buyerId);
    Complaint findByIdAndOrder_Seller_Id(Long complaintId, Long sellerId);
}
