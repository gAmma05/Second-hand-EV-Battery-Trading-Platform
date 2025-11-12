package com.example.SWP.controller.admin;

import com.example.SWP.dto.request.admin.PriorityPackageRequest;
import com.example.SWP.dto.request.admin.SellerPackageRequest;
import com.example.SWP.dto.response.ApiResponse;
import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.entity.SellerPackage;
import com.example.SWP.service.admin.AdminConfigService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/configs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminConfigController {
    AdminConfigService adminConfigService;

    @GetMapping("/seller-packages")
    public ResponseEntity<ApiResponse<List<SellerPackage>>> getAllSellerPackages() {
        List<SellerPackage> responses = adminConfigService.findAllSellerPackages();
        return ResponseEntity.ok(
                ApiResponse.<List<SellerPackage>>builder()
                        .success(true)
                        .message("Lấy danh sách gói bán hàng thành công.")
                        .data(responses)
                        .build()
        );
    }

    @PostMapping("/seller-packages")
    public ResponseEntity<ApiResponse<SellerPackage>> createSellerPackage(
            @Valid @RequestBody SellerPackageRequest request
    ) {
        SellerPackage createdPackage = adminConfigService.createSellerPackage(request);
        return new ResponseEntity<>(
                ApiResponse.<SellerPackage>builder()
                        .success(true)
                        .message("Tạo gói bán hàng thành công.")
                        .data(createdPackage)
                        .build(),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/seller-packages/{id}")
    public ResponseEntity<ApiResponse<SellerPackage>> updateSellerPackage(
            @PathVariable Long id,
            @Valid @RequestBody SellerPackageRequest request) {

        SellerPackage updatedPackage = adminConfigService.updateSellerPackage(id, request);
        return ResponseEntity.ok(
                ApiResponse.<SellerPackage>builder()
                        .success(true)
                        .message("Cập nhật gói bán hàng thành công.")
                        .data(updatedPackage)
                        .build()
        );
    }

    @DeleteMapping("/seller-packages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSellerPackage(@PathVariable Long id) {
        adminConfigService.deleteSellerPackage(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Xóa gói bán hàng thành công.")
                        .build()
        );
    }


    @GetMapping("/priority-packages")
    public ResponseEntity<ApiResponse<List<PriorityPackage>>> getAllPriorityPackages() {
        List<PriorityPackage> responses = adminConfigService.findAllPriorityPackages();
        return ResponseEntity.ok(
                ApiResponse.<List<PriorityPackage>>builder()
                        .success(true)
                        .message("Lấy danh sách gói ưu tiên thành công.")
                        .data(responses)
                        .build()
        );
    }

    @PostMapping("/priority-packages")
    public ResponseEntity<ApiResponse<PriorityPackage>> createPriorityPackage(
            @Valid @RequestBody PriorityPackageRequest request
    ) {
        PriorityPackage createdPackage = adminConfigService.createPriorityPackage(request);
        return new ResponseEntity<>(
                ApiResponse.<PriorityPackage>builder()
                        .success(true)
                        .message("Tạo gói ưu tiên thành công.")
                        .data(createdPackage)
                        .build(),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/priority-packages/{id}")
    public ResponseEntity<ApiResponse<PriorityPackage>> updatePriorityPackage(
            @PathVariable Long id,
            @Valid @RequestBody PriorityPackageRequest request) {

        PriorityPackage updatedPackage = adminConfigService.updatePriorityPackage(id, request);
        return ResponseEntity.ok(
                ApiResponse.<PriorityPackage>builder()
                        .success(true)
                        .message("Cập nhật gói ưu tiên thành công.")
                        .data(updatedPackage)
                        .build()
        );
    }

    @DeleteMapping("/priority-packages/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePriorityPackage(@PathVariable Long id) {
        adminConfigService.deletePriorityPackage(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Xóa gói ưu tiên thành công.")
                        .build()
        );
    }
}