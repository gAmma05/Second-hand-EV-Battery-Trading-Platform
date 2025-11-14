package com.example.SWP.controller.buyer;

import com.example.SWP.dto.request.buyer.CreateComplaintRequest;
import com.example.SWP.dto.request.buyer.RejectComplaintRequest;
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

import java.util.List;

@RestController
@RequestMapping("/buyer/complaints")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerComplaintController {

    BuyerComplaintService buyerComplaintService;

    ComplaintService complaintService;

    @PostMapping("/create")
    public ResponseEntity<?> createComplaint(Authentication authentication, @Valid @RequestBody CreateComplaintRequest request) {
        buyerComplaintService.createComplaint(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Tạo khiếu nại thành công")
                        .build()
        );
    }

    @PostMapping("/admin-request")
    public ResponseEntity<?> adminRequestComplaint(Authentication authentication, @Valid @RequestBody CreateComplaintRequest request) {
        buyerComplaintService.requestToAdmin(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Request đến admin thành công")
                        .build()
        );
    }

    @PatchMapping("/agree")
    public ResponseEntity<?> agreeComplaint(Authentication authentication, @RequestParam Long complaintId) {
        buyerComplaintService.acceptComplaint(authentication, complaintId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Chấp nhận khiếu nại thành công")
                        .build()
        );
    }

    @PatchMapping("/reject")
    public ResponseEntity<?> rejectComplaint(Authentication authentication, @RequestBody RejectComplaintRequest request) {
        buyerComplaintService.rejectComplaint(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Từ chối khiếu nại thành công")
                        .build()
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getComplaintDetail(Authentication authentication, @RequestParam Long complaintId) {
        ComplaintResponse response = complaintService.getComplaintDetail(authentication, complaintId);
        if (response == null) {
            return ResponseEntity.badRequest().body("Không thể truy xuất dữ liệu");
        }
        return ResponseEntity.ok(
                ApiResponse.<ComplaintResponse>builder()
                        .success(true)
                        .message("Truy xuất thông tin khiếu nại thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getComplaintList(Authentication authentication) {
        List<ComplaintResponse> list = buyerComplaintService.getMyComplaints(authentication);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ComplaintResponse>builder()
                            .success(true)
                            .message("Danh sách bị trống")
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponse.<List<ComplaintResponse>>builder()
                        .success(true)
                        .message("Truy xuất danh sách khiếu nại thành công")
                        .data(list)
                        .build()
        );
    }

    @GetMapping("/list/by-order-id")
    public ResponseEntity<?> getComplaintListByOrderId(Authentication authentication, @RequestParam long orderId) {
        List<ComplaintResponse> list = buyerComplaintService.getComplaintsByOrderId(authentication, orderId);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Danh sách bị trống")
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Truy xuất danh sách khiếu nại bằng orderId thành công")
                        .data(list)
                        .build()
        );
    }
}
