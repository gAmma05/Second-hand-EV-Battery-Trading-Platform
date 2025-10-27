package com.example.SWP.controller.buyer;

import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.service.buyer.BuyerOrderDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buyer/order-deliveries")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BuyerOrderDeliveryController {

    BuyerOrderDeliveryService buyerOrderDeliveryService;

    @PostMapping("/{orderId}/confirm-received")
    public ResponseEntity<?> confirmReceived(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        buyerOrderDeliveryService.confirmReceived(authentication, orderId);
        return ResponseEntity.ok("Xác nhận nhận hàng thành công");
    }

    @GetMapping
    public ResponseEntity<List<OrderDeliveryResponse>> getMyDeliveries(Authentication authentication) {
        List<OrderDeliveryResponse> responses = buyerOrderDeliveryService.getMyDeliveries(authentication);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDeliveryResponse> getDeliveryDetail(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        OrderDeliveryResponse response = buyerOrderDeliveryService.getDeliveryDetail(authentication, orderId);
        return ResponseEntity.ok(response);
    }

}
