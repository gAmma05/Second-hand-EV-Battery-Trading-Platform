package com.example.SWP.controller.ghn;

import com.example.SWP.dto.response.ghn.DistrictResponse;
import com.example.SWP.dto.response.ghn.ProvinceResponse;
import com.example.SWP.dto.response.ghn.WardResponse;
import com.example.SWP.service.ghn.GhnAddressService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("user/ghn/address")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class GhnAddressController {
    GhnAddressService ghnAddressService;

    @GetMapping("/provinces")
    public List<ProvinceResponse> getProvinces() {
        return ghnAddressService.getProvinces();
    }

    @GetMapping("/districts/{provinceId}")
    public List<DistrictResponse> getDistricts(@PathVariable int provinceId) {
        return ghnAddressService.getDistricts(provinceId);
    }

    @GetMapping("/wards/{districtId}")
    public List<WardResponse> getWards(@PathVariable int districtId) {
        return ghnAddressService.getWards(districtId);
    }
}
