package com.example.SWP.controller.seller;

import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.service.seller.PriorityPackageService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seller/priority-packages")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PriorityPackageController {

    PriorityPackageService priorityPackageService;

    @GetMapping
    public ResponseEntity<List<PriorityPackage>> getAllPriorityPackages() {
        List<PriorityPackage> packages = priorityPackageService.getAllPriorityPackages();
        return ResponseEntity.ok(packages);
    }
}
