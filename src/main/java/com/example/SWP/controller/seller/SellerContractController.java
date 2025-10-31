package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.request.seller.SignContractRequest;
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
    public ResponseEntity<?> generateContractTemplateByOrder(@PathVariable Long orderId, Authentication authentication) {
        ContractTemplateResponse response = sellerContractService.generateContractTemplate(authentication, orderId);

        return ResponseEntity.ok(
                ApiResponse.<ContractTemplateResponse>builder()
                        .success(true)
                        .message("Fetched contract preview successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<?> createContract(Authentication authentication, @Valid @RequestBody CreateContractRequest request) {
        sellerContractService.createContract(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Created contract successfully")
                        .build()
        );
    }

    @PostMapping("/{contractId}/sign/send-otp")
    public ResponseEntity<?> sendContractOtp(Authentication authentication, @PathVariable Long contractId) {
        sellerContractService.sendContractSignOtp(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("OTP has been sent to your email to sign the contract.")
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
                        .message("Contract has been signed successfully.")
                        .build()
        );
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<?> getContractDetail(Authentication authentication, @PathVariable Long contractId) {
        ContractResponse response = sellerContractService.getContractDetail(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<ContractResponse>builder()
                        .success(true)
                        .message("Contract detail fetched successfully")
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
                        .message("Contract list fetched successfully")
                        .data(response)
                        .build()
        );
    }
}
