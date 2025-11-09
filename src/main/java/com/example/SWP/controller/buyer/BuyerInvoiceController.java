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

    /**
     * Lấy chi tiết một hóa đơn cụ thể của người mua
     */
    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceDetail(
            Authentication authentication,
            @PathVariable Long invoiceId
    ) {
        InvoiceResponse response = buyerInvoiceService.getInvoiceDetail(authentication, invoiceId);
        return ResponseEntity.ok(
                ApiResponse.<InvoiceResponse>builder()
                        .success(true)
                        .message("Lấy thông tin chi tiết hóa đơn thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Lấy tất cả hóa đơn của người mua hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices(Authentication authentication) {
        List<InvoiceResponse> response = buyerInvoiceService.getAllInvoices(authentication);
        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách tất cả hóa đơn thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Lấy tất cả hóa đơn thuộc về một đơn hàng cụ thể
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoicesByOrderId(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        List<InvoiceResponse> response = buyerInvoiceService.getAllInvoicesByOrderId(authentication, orderId);
        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách hóa đơn theo đơn hàng thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Lấy danh sách hóa đơn theo trạng thái (ACTIVE, PAID, EXPIRED, ...)
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStatus(
            Authentication authentication,
            @RequestParam("status") InvoiceStatus status
    ) {
        List<InvoiceResponse> response = buyerInvoiceService.getInvoicesByStatus(authentication, status);
        return ResponseEntity.ok(
                ApiResponse.<List<InvoiceResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách hóa đơn theo trạng thái " + status + " thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Thanh toán hóa đơn
     */
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<Void>> payInvoice(
            Authentication authentication,
            @Valid @RequestBody PayInvoiceRequest request
    ) {
        buyerInvoiceService.payInvoice(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Thanh toán hóa đơn thành công")
                        .build()
        );
    }
}
