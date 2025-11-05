package com.example.SWP.controller.seller;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.service.seller.SellerStatsService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/seller/stats")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerStatsController {

    SellerStatsService sellerStatsService;

    @GetMapping("/orders/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getOrderCountByStatus(
            Authentication authentication,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {

        Map<String, Long> responses = sellerStatsService.getOrderCountByStatus(authentication, year, month);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Long>>builder()
                        .success(true)
                        .message("Thống kê số lượng đơn hàng theo trạng thái thành công")
                        .data(responses)
                        .build()
        );
    }

    @GetMapping("/posts/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPostCountByStatus(
            Authentication authentication,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {

        Map<String, Long> responses = sellerStatsService.getPostCountByStatus(authentication, year, month);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Long>>builder()
                        .success(true)
                        .message("hống kê số lượng bài đăng theo trạng thái thành công")
                        .data(responses)
                        .build()
        );
    }
}
