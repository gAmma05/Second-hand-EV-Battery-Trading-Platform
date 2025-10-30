package com.example.SWP.controller.user;

import com.example.SWP.dto.request.user.wallet.DepositRequest;
import com.example.SWP.dto.request.user.wallet.WithdrawRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.PaymentStatus;
import com.example.SWP.enums.TransactionType;
import com.example.SWP.service.user.WalletService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/user/wallet")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletController {

    WalletService walletService;

    // Tạo VNPay để nạp tiền
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<String>> deposit(
            Authentication authentication,
            @Valid @RequestBody DepositRequest request) {

        // Gọi service để tạo URL thanh toán
        String paymentUrl = walletService.deposit(authentication, request.getAmount());

        // Trả về ApiResponse có chứa paymentUrl
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Payment URL generated successfully")
                .data(paymentUrl)
                .build();

        return ResponseEntity.ok(response);
    }


    //Xử lý VNPay return
    @GetMapping("/vnpay-return/deposit")
    public RedirectView handleDepositVNPayReturn(
            @RequestParam String vnp_TxnRef,
            @RequestParam String vnp_ResponseCode,
            @RequestParam(required = false) String vnp_BankCode
    ) {
        try {
            WalletTransaction transaction = walletService.handleDepositVNPayReturn(vnp_TxnRef, vnp_ResponseCode, vnp_BankCode);

            boolean isSuccess = transaction.getStatus() == PaymentStatus.SUCCESS;

            String redirectUrl = "http://localhost:5173/user/wallet";
            if (isSuccess) {
                redirectUrl += "?paymentStatus=success";
            } else {
                redirectUrl += "?paymentStatus=failed";
            }

            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            String redirectUrl = "http://localhost:5173/user/wallet?paymentStatus=error";
            return new RedirectView(redirectUrl);
        }
    }


    //Xu li rut tien
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            Authentication authentication,
            @Valid @RequestBody WithdrawRequest request) {

        try {
            WalletTransaction transaction = walletService.withdraw(
                    authentication,
                    request.getAmount(),
                    request.getBankAccount()
            );

            boolean isSuccess = transaction.getStatus() == PaymentStatus.SUCCESS;

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(isSuccess)
                    .message(isSuccess ? "Withdrawal successful" : "Withdrawal request submitted")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Withdrawal error: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(Authentication authentication) {
        BigDecimal balance = walletService.getBalance(authentication);
        ApiResponse<BigDecimal> response = ApiResponse.<BigDecimal>builder()
                .success(true)
                .message("Current balance fetched successfully")
                .data(balance)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<WalletTransaction> transactions = walletService.getTransactions(authentication, page, size);

        ApiResponse<List<WalletTransaction>> response = ApiResponse.<List<WalletTransaction>>builder()
                .success(true)
                .message("Transaction history fetched successfully")
                .data(transactions)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<WalletTransaction>> getTransactionDetail(
            Authentication authentication,
            @PathVariable Long id) {

        try {
            WalletTransaction transaction = walletService.getTransactionDetail(authentication, id);

            ApiResponse<WalletTransaction> response = ApiResponse.<WalletTransaction>builder()
                    .success(true)
                    .message("Transaction detail fetched successfully")
                    .data(transaction)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<WalletTransaction> errorResponse = ApiResponse.<WalletTransaction>builder()
                    .success(false)
                    .message("Error fetching transaction: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/transactions/type")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getTransactionsByType(
            Authentication authentication,
            @RequestParam TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<WalletTransaction> transactions = walletService.getTransactionsByType(authentication, type, page, size);
        ApiResponse<List<WalletTransaction>> response = ApiResponse.<List<WalletTransaction>>builder()
                .success(true)
                .message("Filtered transaction history fetched successfully")
                .data(transactions)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/status")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getTransactionsByStatus(
            Authentication authentication,
            @RequestParam PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<WalletTransaction> transactions = walletService.getTransactionsByStatus(authentication, status, page, size);
        ApiResponse<List<WalletTransaction>> response = ApiResponse.<List<WalletTransaction>>builder()
                .success(true)
                .message("Filtered transaction history fetched successfully")
                .data(transactions)
                .build();
        return ResponseEntity.ok(response);
    }

}
