package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.service.seller.SellerOrderDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/order-deliveries")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SellerOrderDeliveryController {
    SellerOrderDeliveryService sellerOrderDeliveryService;

    @GetMapping
    public ResponseEntity<List<OrderDeliveryResponse>> getMyDeliveries(Authentication authentication) {
        List<OrderDeliveryResponse> responses = sellerOrderDeliveryService.getMyDeliveries(authentication);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDeliveryResponse> getDeliveryDetail(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        OrderDeliveryResponse response = sellerOrderDeliveryService.getDeliveryDetail(authentication, orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/manual")
    public ResponseEntity<ApiResponse<OrderDeliveryResponse>> updateManualStatus(
            @PathVariable Long orderId,
            @RequestParam DeliveryStatus deliveryStatus
    ) {
        OrderDeliveryResponse response = sellerOrderDeliveryService.updateManualDeliveryStatus(orderId, deliveryStatus);
        return ResponseEntity.ok(
                ApiResponse.<OrderDeliveryResponse>builder()
                        .success(true)
                        .message("Manual delivery status updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{orderId}/ghn")
    public ResponseEntity<ApiResponse<OrderDeliveryResponse>> updateGhnStatus(
            @PathVariable Long orderId
    ) {
        OrderDeliveryResponse response = sellerOrderDeliveryService.updateGhnDeliveryStatus(orderId);
        return ResponseEntity.ok(
                ApiResponse.<OrderDeliveryResponse>builder()
                        .success(true)
                        .message("GHN delivery status synced successfully")
                        .data(response)
                        .build()
        );
    }
}
