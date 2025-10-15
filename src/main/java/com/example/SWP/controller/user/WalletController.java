package com.example.SWP.controller.user;

import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.service.user.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/user/wallet")
@RequiredArgsConstructor
public class WalletController {

    WalletService walletService;

    //Tạo VNPay để nạp tiền
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(Principal principal, @RequestParam BigDecimal amount) {
        String paymentUrl = walletService.deposit(principal.getName(), amount);
        return ResponseEntity.ok(paymentUrl);
    }

    //Xu li VNPay Return
    @GetMapping("/vnpay-return/deposit")
    public ResponseEntity<WalletTransaction> handleDepositVNPayReturn(
            @RequestParam String vnp_TxnRef,
            @RequestParam String vnp_ResponseCode,
            @RequestParam(required = false) String vnp_BankCode) {
        WalletTransaction tx = walletService.handleDepositVNPayReturn(vnp_TxnRef, vnp_ResponseCode, vnp_BankCode);
        return ResponseEntity.ok(tx);
    }
}
