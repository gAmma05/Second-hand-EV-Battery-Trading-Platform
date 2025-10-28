package com.example.SWP.controller.buyer;

import com.example.SWP.dto.request.seller.PayInvoiceRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.enums.InvoiceStatus;
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

    BuyerInvoiceService buyerInvoiceService;

    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestParam Long contractId) {
        buyerInvoiceService.createInvoice(contractId);
        return ResponseEntity.ok(
                ApiResponse.<InvoiceResponse>builder()
                        .success(true)
                        .message("Created invoice successfully")
                        .build()
        );
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoiceDetail(Authentication authentication, @PathVariable Long invoiceId) {
        InvoiceResponse response = buyerInvoiceService.getInvoiceDetail(authentication, invoiceId);
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

    @GetMapping
    public ResponseEntity<?> getAllInvoices(Authentication authentication) {
        List<InvoiceResponse> response = buyerInvoiceService.getAllInvoices(authentication);

        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("All invoices retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getAllInvoicesByOrderId(Authentication authentication, @PathVariable Long orderId) {
        List<InvoiceResponse> response = buyerInvoiceService.getAllInvoicesByOrderId(authentication, orderId);

        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("All invoices retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/status")
    public ResponseEntity<?> getInvoicesByStatus(
            Authentication authentication,
            @RequestParam("status") InvoiceStatus status
    ) {
        List<InvoiceResponse> response = buyerInvoiceService.getInvoicesByStatus(authentication, status);

        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("Fetched " + status + " invoices successfully")
                        .data(response)
                        .build()
        );
    }


    @PostMapping("/pay")
    public ResponseEntity<?> payInvoice(Authentication authentication, @Valid @RequestBody PayInvoiceRequest request) {
        buyerInvoiceService.payInvoice(authentication, request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Invoice paid successfully")
                        .data(null)
                        .build()
        );
    }
}
