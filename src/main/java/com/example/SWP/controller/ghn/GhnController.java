package com.example.SWP.controller.ghn;

import com.example.SWP.dto.request.ghn.ServiceRequest;
import com.example.SWP.dto.request.ghn.FeeRequest;
import com.example.SWP.dto.response.ghn.*;
import com.example.SWP.entity.User;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.ghn.GhnService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ghn")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class GhnController {
    GhnService ghnService;
    private final UserRepository userRepository;

    @GetMapping("/address/provinces")
    public List<ProvinceResponse> getProvinces() {
        return ghnService.getProvinces();
    }

    @GetMapping("/address/districts/{provinceId}")
    public List<DistrictResponse> getDistricts(@PathVariable int provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @GetMapping("/address/wards/{districtId}")
    public List<WardResponse> getWards(@PathVariable int districtId) {
        return ghnService.getWards(districtId);
    }

    @PostMapping("buyer/available-services")
    public List<AvailableServicesResponse> getAvailableServices(@RequestBody ServiceRequest request, Authentication authentication) {
        return ghnService.getAvailableServices(request, authentication);
    }

    @PostMapping("buyer/shipping-fee")
    public FeeResponse calculateShippingFee(@RequestBody FeeRequest request, Authentication authentication) {
        String email = authentication.getName();
        User buyer = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return ghnService.calculateShippingFee(request, buyer);
    }


}
