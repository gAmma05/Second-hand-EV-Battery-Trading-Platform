package com.example.SWP.controller.ghn;

import com.example.SWP.dto.request.ghn.ServiceRequest;
import com.example.SWP.dto.request.ghn.FeeRequest;
import com.example.SWP.dto.response.ghn.*;
import com.example.SWP.service.ghn.GhnService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("user/ghn")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class GhnController {
    GhnService ghnService;

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

    @PostMapping("/available-services")
    public List<AvailableServicesResponse> getAvailableServices(@RequestBody ServiceRequest request) {
        return ghnService.getAvailableServices(request);
    }

    @PostMapping("/shipping-fee")
    public FeeResponse calculateShippingFee(@RequestBody FeeRequest request) {
        return ghnService.calculateShippingFee(request);
    }

}
