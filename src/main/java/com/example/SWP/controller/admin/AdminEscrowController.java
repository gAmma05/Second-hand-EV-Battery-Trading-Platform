package com.example.SWP.controller.admin;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.admin.EscrowResponse;
import com.example.SWP.dto.response.admin.EscrowTransactionResponse;
import com.example.SWP.service.escrow.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/escrow")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminEscrowController {

    EscrowService escrowService;

    @GetMapping("/list")
    public ResponseEntity<?> getEscrowList() {
        List<EscrowResponse> responses = escrowService.getEscrowList();
        return ResponseEntity.ok(
                ApiResponse.<List<EscrowResponse>>builder()
                        .success(true)
                        .message("Truy xuất dữ liệu escrow thành công")
                        .data(responses)
                        .build()
        );
    }

    @GetMapping("/transaction/list")
    public ResponseEntity<?> getEscrowTransactionList() {
        List<EscrowTransactionResponse> responses = escrowService.getEscrowTransactionList();
        return ResponseEntity.ok(
                ApiResponse.<List<EscrowTransactionResponse>>builder()
                        .success(true)
                        .message("Truy xuất dữ liệu escrow thành công")
                        .data(responses)
                        .build()
        );
    }
}
