package com.example.SWP.controller.seller;

import com.example.SWP.dto.request.seller.CreateContractRequest;
import com.example.SWP.dto.request.seller.SignContractRequest;
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


    @PostMapping("/create")
    public ResponseEntity<?> createContract(Authentication authentication, @Valid @RequestBody CreateContractRequest request) {
        sellerContractService.createContract(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Created contract successfully")
                        .build()
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getContractDetail(Authentication authentication, @RequestParam Long contractId) {
        ContractResponse response = sellerContractService.getContractDetail(authentication, contractId);
        if (response == null) {
            return ResponseEntity.badRequest().body("Failed to fetch contract detail");
        }
        return ResponseEntity.ok(
                ApiResponse.<ContractResponse>builder()
                        .success(true)
                        .message("Contract detail fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getContractList(Authentication authentication) {
        List<ContractResponse> response = sellerContractService.getAllContracts(authentication);
        if (response == null || response.isEmpty()) {
            return ResponseEntity.badRequest().body("Failed to fetch contract list");
        }
        return ResponseEntity.ok(
                ApiResponse.<List<ContractResponse>>builder()
                        .success(true)
                        .message("Contract list fetched successfully")
                        .data(response)
                        .build()
        );
    }
}
