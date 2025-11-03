package com.example.SWP.controller.admin;

import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.dto.response.admin.StatsResponse;
import com.example.SWP.service.admin.AdminStatsService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatsController {

    AdminStatsService adminStatsService;

    @GetMapping()
    public ResponseEntity<?> getStats() {
        StatsResponse response = adminStatsService.getStats();
        return ResponseEntity.ok(
                ApiResponse.<StatsResponse>builder()
                        .success(true)
                        .message("Fetched stats successfully")
                        .data(response)
                        .build()
        );
    }
}
