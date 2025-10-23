package com.example.SWP.controller.buyer;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.buyer.ContractResponse;
import com.example.SWP.service.buyer.BuyerContractService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/buyer/contracts")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerContractController {

    BuyerContractService buyerContractService;

    @GetMapping("/approve")
    public ResponseEntity<?> approveContract(Authentication authentication, @RequestParam Long contractId) {
        buyerContractService.signContract(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Contract approved successfully")
                        .build()
        );
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> cancelContract(Authentication authentication, @RequestParam Long contractId) {
        buyerContractService.cancelContract(authentication, contractId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Contract cancelled successfully")
                        .build()
        );
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getContractDetail(Authentication authentication, @RequestParam Long contractId) {
        ContractResponse contractResponse = buyerContractService.getContractDetail(authentication, contractId);
        if (contractResponse == null) {
            return ResponseEntity.badRequest().body("Failed to fetch contract detail");
        }

        return ResponseEntity.ok(
                ApiResponse.<ContractResponse>builder()
                        .success(true)
                        .message("Contract detail fetched successfully")
                        .data(contractResponse)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getContractList(Authentication authentication){
        List<ContractResponse> contractList = buyerContractService.getAllContractsSignedBySeller(authentication);
        if(contractList == null || contractList.isEmpty()){
            return ResponseEntity.badRequest().body("Failed to fetch contract list");
        }

        return ResponseEntity.ok(
                ApiResponse.<List<ContractResponse>>builder()
                        .success(true)
                        .message("Contract list fetched successfully")
                        .data(contractList)
                        .build()
        );
    }
}
