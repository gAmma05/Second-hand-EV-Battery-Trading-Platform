package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.ComplaintResolutionRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.service.complaint.ComplaintService;
import com.example.SWP.service.seller.SellerComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/complaints")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerComplaintController {

    SellerComplaintService sellerComplaintService;

    ComplaintService complaintService;

    @PatchMapping("/accept")
    public ResponseEntity<?> acceptComplaintRequest(Authentication authentication, @RequestParam Long complaintId) {
        sellerComplaintService.acceptComplaint(authentication, complaintId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Complaint accepted successfully")
                        .build()
        );
    }

    @PatchMapping("/admin-request")
    public ResponseEntity<?> adminRequestComplaint(Authentication authentication, @RequestParam Long complaintId) {
        sellerComplaintService.requestToAdmin(authentication, complaintId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Complaint requested to admin successfully")
                        .build()
        );
    }

    @PatchMapping("/resolution")
    public ResponseEntity<?> resolveComplaint(Authentication authentication, @Valid @RequestBody ComplaintResolutionRequest request) {
        sellerComplaintService.responseComplaint(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Complaint resolved successfully")
                        .build()
        );
    }

    @PostMapping("/detail")
    public ResponseEntity<?> getComplaintDetail(Authentication authentication, @RequestParam Long complaintId) {
        ComplaintResponse response = complaintService.getComplaintDetail(authentication, complaintId);
        if (response == null) {
            return ResponseEntity.badRequest().body("Failed to fetch complaint detail");
        }
        return ResponseEntity.ok(
                ApiResponse.<ComplaintResponse>builder()
                        .success(true)
                        .message("Complaint detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getComplaintList(Authentication authentication) {
        List<ComplaintResponse> list = sellerComplaintService.getMyComplaints(authentication);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ComplaintResponse>builder()
                            .success(false)
                            .message("List is empty")
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponse.<List<ComplaintResponse>>builder()
                        .success(true)
                        .message("Fetched complaint list successfully")
                        .data(list)
                        .build()
        );
    }
}
