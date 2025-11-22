package com.example.SWP.service.scheduler;

import com.example.SWP.entity.*;
import com.example.SWP.entity.escrow.Escrow;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.EscrowStatus;
import com.example.SWP.repository.*;
import com.example.SWP.service.escrow.EscrowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComplaintScheduler {

    ComplaintRepository complaintRepository;

    EscrowService escrowService;

    @Scheduled(cron = "0 */1 * * * *")
    public void autoReqComplaintToAdmin() {
        int CHECK_DAYS = 7;
        LocalDateTime today = LocalDateTime.now();
        List<Complaint> complaintList = complaintRepository.findByStatusIn(List.of(
                ComplaintStatus.SELLER_REVIEWING,
                ComplaintStatus.BUYER_REJECTED,
                ComplaintStatus.SELLER_REJECTED,
                ComplaintStatus.SELLER_RESOLVED
        ));
        log.info("Running complaint auto-request job for {} complaints", complaintList.size());

        for (Complaint complaint : complaintList) {
            try {
                if (complaint.getStatus() == ComplaintStatus.SELLER_REVIEWING
                        || complaint.getStatus() == ComplaintStatus.BUYER_REJECTED) {

                    if (ChronoUnit.DAYS.between(complaint.getCreatedAt(), today) > CHECK_DAYS) {
                        complaint.setStatus(ComplaintStatus.ADMIN_REVIEWING);
                        log.info("Keep complaint {} status as {}", complaint.getId(), complaint.getStatus());
                        complaint.setUpdatedAt(LocalDateTime.now());
                    }


                } else if (complaint.getStatus() == ComplaintStatus.SELLER_REJECTED
                        || complaint.getStatus() == ComplaintStatus.SELLER_RESOLVED) {

                    if (ChronoUnit.DAYS.between(complaint.getCreatedAt(), today) > CHECK_DAYS) {
                        complaint.setStatus(ComplaintStatus.CLOSED_NO_REFUND);
                        escrowService.switchStatus(EscrowStatus.RELEASED_TO_SELLER, complaint.getOrder().getId());
                        complaint.setUpdatedAt(LocalDateTime.now());
                        log.info("Updated complaint {} status to {}", complaint.getId(), complaint.getStatus());
                    } else {
                        log.info("Keep complaint {} status as {}", complaint.getId(), complaint.getStatus());
                    }
                }

                complaintRepository.save(complaint);


            } catch (Exception e) {
                log.error("Error while processing complaint {}: {}", complaint.getId(), e.getMessage());
            }
        }
    }
}
