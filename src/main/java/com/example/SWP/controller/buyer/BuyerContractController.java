package com.example.SWP.controller.buyer;

import com.example.SWP.dto.request.user.VerifyContractSignatureRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.service.buyer.BuyerContractService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/buyer/contracts")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerContractController {

    BuyerContractService buyerContractService;

    @PatchMapping("/{contractId}/sign/send-otp")
    public ResponseEntity<?> sendContractSignOtp(Authentication authentication,
                                                 @PathVariable Long contractId) {
        buyerContractService.sendContractSignOtp(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("OTP has been sent to your email. Please check and verify.")
                        .build()
        );
    }

    // Xác minh OTP và ký hợp đồng
    @PatchMapping("/sign/verify")
    public ResponseEntity<?> verifyContractSignOtp(Authentication authentication,
                                                   @RequestBody VerifyContractSignatureRequest request) {
        buyerContractService.verifyContractSignOtp(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Contract signed successfully.")
                        .build()
        );
    }

    @PatchMapping("/{contractId}/cancel")
    public ResponseEntity<?> cancelContract(Authentication authentication, @PathVariable Long contractId) {
        buyerContractService.cancelContract(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Contract cancelled successfully")
                        .build()
        );
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<?> getContractDetail(Authentication authentication, @PathVariable Long contractId) {
        ContractResponse contractResponse = buyerContractService.getContractDetail(authentication, contractId);

        return ResponseEntity.ok(
                ApiResponse.<ContractResponse>builder()
                        .success(true)
                        .message("Contract detail fetched successfully")
                        .data(contractResponse)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<?> getContractList(Authentication authentication){
        List<ContractResponse> contractList = buyerContractService.getAllContracts(authentication);

        return ResponseEntity.ok(
                ApiResponse.<List<ContractResponse>>builder()
                        .success(true)
                        .message("Contract list fetched successfully")
                        .data(contractList)
                        .build()
        );
    }
}
