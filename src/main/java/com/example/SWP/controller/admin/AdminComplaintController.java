package com.example.SWP.controller.admin;

import com.example.SWP.dto.request.admin.HandleComplaintRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.service.admin.AdminComplaintService;
import com.example.SWP.service.complaint.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/complaints")
public class AdminComplaintController {

    AdminComplaintService adminComplaintService;

    ComplaintService complaintService;

    @PatchMapping("/handle")
    public ResponseEntity<?> approveComplaint(@RequestBody HandleComplaintRequest request) {
        adminComplaintService.handleComplaint(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Complaint handled successfully")
                        .build()
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getComplaintDetail(Authentication authentication, @RequestParam Long complaintId) {
        ComplaintResponse response = complaintService.getComplaintDetail(authentication, complaintId);
        return ResponseEntity.ok(
                ApiResponse.<ComplaintResponse>builder()
                        .success(true)
                        .message("Complaint detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getComplaintList() {
        List<ComplaintResponse> response = adminComplaintService.getMyComplaints();
        if (response == null || response.isEmpty()) {
            return ResponseEntity.ok(
                    ApiResponse.<ComplaintResponse>builder()
                            .success(true)
                            .message("List is empty")
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.<List<ComplaintResponse>>builder()
                        .success(true)
                        .message("Fetched complaint list successfully")
                        .data(response)
                        .build()
        );
    }
}
