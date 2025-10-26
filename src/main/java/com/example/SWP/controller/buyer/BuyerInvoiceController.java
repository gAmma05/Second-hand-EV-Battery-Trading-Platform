package com.example.SWP.controller.buyer;

import com.example.SWP.dto.request.seller.PayInvoiceRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.service.buyer.BuyerInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buyer/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerInvoiceController {

    BuyerInvoiceService buyerPaymentService;

    @GetMapping("/detail")
    public ResponseEntity<?> getInvoiceDetail(Authentication authentication, Long invoiceId) {
        InvoiceResponse response = buyerPaymentService.getInvoiceDetail(authentication, invoiceId);
        if (response == null) {
            return ResponseEntity.badRequest().body("Failed to fetch this invoice detail");
        }
        return ResponseEntity.ok(
                ApiResponse.<InvoiceResponse>builder()
                        .success(true)
                        .message("Fetched invoice detail successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/full-list")
    public ResponseEntity<?> getAllInvoice(Authentication authentication) {
        List<InvoiceResponse> response = buyerPaymentService.getAllInvoices(authentication);
        if (response == null || response.isEmpty()) {
            return ResponseEntity.badRequest().body("Failed to fetch invoice list or you don't have any invoice yet");
        }
        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("Fetched invoice list successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/expired-list")
    public ResponseEntity<?> getExpiredList(Authentication authentication) {
        List<InvoiceResponse> response = buyerPaymentService.getExpiredInvoices(authentication);
        if (response == null || response.isEmpty()) {
            return ResponseEntity.badRequest().body("Failed to fetch expired invoice list or you don't have any expired invoice yet");
        }
        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("Fetched expired invoice list successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/valid-list")
    public ResponseEntity<?> getValidList(Authentication authentication) {
        List<InvoiceResponse> response = buyerPaymentService.getValidInvoices(authentication);
        if (response == null || response.isEmpty()) {
            return null;
        }
        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("Fetched valid invoice list successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/pay")
    public ResponseEntity<?> payInvoice(Authentication authentication, @Valid @RequestBody PayInvoiceRequest request) {
        buyerPaymentService.payInvoice(authentication, request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Invoice paid successfully")
                        .data(null)
                        .build()
        );
    }


}
