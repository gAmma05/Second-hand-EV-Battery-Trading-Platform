package com.example.SWP.service.ghn;

import com.example.SWP.dto.response.ghn.DistrictResponse;
import com.example.SWP.dto.response.ghn.ProvinceResponse;
import com.example.SWP.dto.response.ghn.WardResponse;
import com.example.SWP.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class GhnAddressService {

    final RestTemplate restTemplate;
    @Value("${ghn.token}")
    String GHN_TOKEN;
    @Value("${ghn.url}")
    String GHN_URL;

    public List<ProvinceResponse> getProvinces() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                GHN_URL + "/master-data/province",
                HttpMethod.GET,
                entity,
                Map.class
        );

        List<Map<String,Object>> data = (List<Map<String,Object>>) response.getBody().get("data");
        List<ProvinceResponse> provinces = new ArrayList<>();
        if (data != null) {
            for (Map<String,Object> item : data) {
                ProvinceResponse p = new ProvinceResponse();
                Object idObj = item.get("ProvinceID");
                if (idObj != null) p.setProvinceId(Integer.parseInt(idObj.toString()));
                p.setProvinceName((String) item.get("ProvinceName"));
                provinces.add(p);
            }
        }
        return provinces;
    }

    public List<DistrictResponse> getDistricts(int provinceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                GHN_URL + "/master-data/district?province_id=" + provinceId,
                HttpMethod.GET,
                entity,
                Map.class
        );

        List<Map<String,Object>> data = (List<Map<String,Object>>) response.getBody().get("data");
        List<DistrictResponse> districts = new ArrayList<>();
        if (data != null) {
            for (Map<String,Object> item : data) {
                DistrictResponse d = new DistrictResponse();
                Object idObj = item.get("DistrictID");
                if (idObj != null) d.setDistrictId(Integer.parseInt(idObj.toString()));
                d.setDistrictName((String) item.get("DistrictName"));
                districts.add(d);
            }
        }
        return districts;
    }

    public List<WardResponse> getWards(int districtId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                GHN_URL + "/master-data/ward?district_id=" + districtId,
                HttpMethod.GET,
                entity,
                Map.class
        );

        List<Map<String,Object>> data = (List<Map<String,Object>>) response.getBody().get("data");
        List<WardResponse> wards = new ArrayList<>();
        if (data != null) {
            for (Map<String,Object> item : data) {
                WardResponse w = new WardResponse();
                Object idObj = item.get("WardCode");
                if (idObj != null) w.setWardId(Integer.parseInt(idObj.toString()));
                w.setWardName((String) item.get("WardName"));
                wards.add(w);
            }
        }
        return wards;
    }

    public void validateAddressIds(Integer provinceId, Integer districtId, Integer wardId) {
        if (provinceId == null || districtId == null || wardId == null) {
            throw new BusinessException("Province, district hoặc ward không được để trống", 400);
        }

        List<DistrictResponse> districts;
        try {
            districts = getDistricts(provinceId);
        } catch (Exception e) {
            throw new BusinessException("Không tìm thấy districts cho ProvinceID: " + provinceId, 400);
        }

        boolean districtBelongs = districts.stream()
                .anyMatch(d -> Objects.equals(d.getDistrictId(), districtId));
        if (!districtBelongs) {
            throw new BusinessException("DistrictID không thuộc ProvinceID", 400);
        }

        List<WardResponse> wards;
        try {
            wards = getWards(districtId);
        } catch (Exception e) {
            throw new BusinessException("Không tìm thấy wards cho DistrictID: " + districtId, 400);
        }

        boolean wardBelongs = wards.stream()
                .anyMatch(w -> Objects.equals(w.getWardId(), wardId));
        if (!wardBelongs) {
            throw new BusinessException("WardID không thuộc DistrictID", 400);
        }
    }


}
