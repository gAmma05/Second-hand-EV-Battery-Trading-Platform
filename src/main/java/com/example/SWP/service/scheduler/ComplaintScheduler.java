package com.example.SWP.service.scheduler;

import com.example.SWP.entity.*;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.repository.*;
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

    @Scheduled(cron = "0 0 0 * * *")
    public void autoReqComplaintToAdmin() {
        int CHECK_DAYS = 7;
        LocalDateTime today = LocalDateTime.now();
        List<Complaint> complaintList = complaintRepository.findAll();
        log.info("Running complaint auto-request job for {} complaints", complaintList.size());

        for (Complaint complaint : complaintList) {
            try {
                if (complaint.getStatus() == ComplaintStatus.PENDING) {
                    if (complaint.getUpdatedAt() == null) {
                        if (ChronoUnit.DAYS.between(complaint.getCreatedAt(), today) >= CHECK_DAYS) {
                            complaint.setStatus(ComplaintStatus.ADMIN_SOLVING);
                        }
                    } else {
                        if (ChronoUnit.DAYS.between(complaint.getUpdatedAt(), today) >= CHECK_DAYS) {
                            complaint.setStatus(ComplaintStatus.ADMIN_SOLVING);
                        }
                    }
                    complaint.setUpdatedAt(LocalDateTime.now());
                }
                complaintRepository.save(complaint);
                log.info("Updated complaint {} status to {}", complaint.getId(), complaint.getStatus());
                complaintList.clear();
                log.info("Clearing complaint list");
            } catch (Exception e) {
                log.error("Error while processing order {}: {}", complaint.getId(), e.getMessage());
            }
        }
    }
}
