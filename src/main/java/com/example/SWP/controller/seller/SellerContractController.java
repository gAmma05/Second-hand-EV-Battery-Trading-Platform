package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.request.seller.SignContractRequest;
import com.example.SWP.dto.request.seller.UpdateContractRequest;
import com.example.SWP.dto.request.user.VerifyContractSignatureRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.seller.ContractTemplateResponse;
import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.service.seller.SellerContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/contracts")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerContractController {

    SellerContractService sellerContractService;

    @GetMapping("/template/{orderId}")
    public ResponseEntity<?> generateContractTemplateByOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        ContractTemplateResponse response = sellerContractService.generateContractTemplate(authentication, orderId);

        return ResponseEntity.ok(
                ApiResponse.<ContractTemplateResponse>builder()
                        .success(true)
                        .message("Lấy bản xem trước hợp đồng thành công")
                        .data(response)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<?> createContract(
            Authentication authentication,
            @Valid @RequestBody CreateContractRequest request
    ) {
        sellerContractService.createContract(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Tạo hợp đồng thành công")
                        .build()
        );
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<?> updateContract(
            Authentication authentication,
            @PathVariable Long contractId,
            @Valid @RequestBody UpdateContractRequest request
    ) {
        sellerContractService.updateContract(authentication, contractId, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cập nhật hợp đồng thành công")
                        .build()
        );
    }



    @PostMapping("/{contractId}/sign/send-otp")
    public ResponseEntity<?> sendContractOtp(
            Authentication authentication,
            @PathVariable Long contractId
    ) {
        sellerContractService.sendContractSignOtp(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("OTP đã được gửi đến email của bạn để ký hợp đồng")
                        .build()
        );
    }

    @PostMapping("/sign/verify")
    public ResponseEntity<?> verifyContractSignature(
            Authentication authentication,
            @Valid @RequestBody VerifyContractSignatureRequest request
    ) {
        sellerContractService.verifyContractSignOtp(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Hợp đồng đã được ký thành công")
                        .build()
        );
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<?> getContractDetail(
            Authentication authentication,
            @PathVariable Long contractId
    ) {
        ContractResponse response = sellerContractService.getContractDetail(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<ContractResponse>builder()
                        .success(true)
                        .message("Chi tiết hợp đồng đã được lấy thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<?> getContractList(Authentication authentication) {
        List<ContractResponse> response = sellerContractService.getAllContracts(authentication);

        return ResponseEntity.ok(
                ApiResponse.<List<ContractResponse>>builder()
                        .success(true)
                        .message("Danh sách hợp đồng đã được lấy thành công")
                        .data(response)
                        .build()
        );
    }
}
