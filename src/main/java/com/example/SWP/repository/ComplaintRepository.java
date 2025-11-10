package com.example.SWP.repository;

import com.example.SWP.entity.Complaint;
import com.example.SWP.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByOrder_Buyer_Id(Long buyerId);

    List<Complaint> findByOrder_Seller_Id(Long sellerId);

    List<Complaint> findByStatus(ComplaintStatus status);

    Complaint findByIdAndOrder_Buyer_Id(Long complaintId, Long buyerId);

    Complaint findByIdAndOrder_Seller_Id(Long complaintId, Long sellerId);

    Optional<Complaint> findByOrder_Id(Long orderId);

    int countComplaintByStatus(ComplaintStatus status);

    int countComplaintByOrderId(Long orderId);

    double countComplaintByOrderIdAndStatus(Long orderId, ComplaintStatus status);

    List<Complaint> findByOrder_IdAndOrder_Buyer_Id(Long orderId, Long id);

    List<Complaint> findByOrder_IdAndOrder_Seller_Id(Long orderId, Long id);
}
