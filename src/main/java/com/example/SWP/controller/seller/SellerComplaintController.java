package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.ComplaintRequest;
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

    @PatchMapping("/resolution")
    public ResponseEntity<?> resolveComplaint(Authentication authentication, @Valid @RequestBody ComplaintRequest request) {
        sellerComplaintService.responseComplaint(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Đã cập nhật khiếu nại")
                        .build()
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getComplaintDetail(Authentication authentication, @RequestParam Long complaintId) {
        ComplaintResponse response = complaintService.getComplaintDetail(authentication, complaintId);
        if (response == null) {
            return ResponseEntity.badRequest().body("Có lỗi khi truy xuất khiếu nại");
        }
        return ResponseEntity.ok(
                ApiResponse.<ComplaintResponse>builder()
                        .success(true)
                        .message("Truy xuất khiếu nại thành công")
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
        List<ComplaintResponse> list = sellerComplaintService.getComplaintsByOrderId(authentication, orderId);
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
