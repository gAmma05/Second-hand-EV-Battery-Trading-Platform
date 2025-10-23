package com.example.SWP.controller.ghn;

import com.example.SWP.dto.request.ghn.GhnAvailableServiceRequest;
import com.example.SWP.dto.request.ghn.GhnShippingFeeRequest;
import com.example.SWP.dto.response.ghn.DistrictResponse;
import com.example.SWP.dto.response.ghn.ProvinceResponse;
import com.example.SWP.dto.response.ghn.WardResponse;
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
    public Object getAvailableServices(@RequestBody GhnAvailableServiceRequest request) {
        return ghnService.getAvailableServices(
                request.getFromDistrictId(),
                request.getToDistrictId()
        );
    }

    @PostMapping("/shipping-fee")
    public Object calculateShippingFee(@RequestBody GhnShippingFeeRequest request) {
        return ghnService.calculateShippingFee(
                request.getFromDistrictId(),
                request.getToDistrictId(),
                request.getToWardCode(),
                request.getServiceId(),
                request.getWeight()

        );
    }

}
