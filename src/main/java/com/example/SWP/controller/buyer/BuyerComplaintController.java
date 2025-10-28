package com.example.SWP.controller.buyer;

import com.example.SWP.dto.request.buyer.CreateComplaintRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.service.buyer.BuyerComplaintService;
import com.example.SWP.service.complaint.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buyer/complaints")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerComplaintController {

    BuyerComplaintService buyerComplaintService;

    ComplaintService complaintService;

    @PostMapping("/create")
    public ResponseEntity<?> createComplaint(Authentication authentication, @Valid CreateComplaintRequest request) {
        buyerComplaintService.createComplaint(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Created complaint successfully")
                        .build()
        );
    }

    @GetMapping("/detail")
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
}
