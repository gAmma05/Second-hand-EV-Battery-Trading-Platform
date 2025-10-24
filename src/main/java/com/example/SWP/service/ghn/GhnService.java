package com.example.SWP.service.ghn;

import com.example.SWP.dto.request.ghn.GhnShippingFeeRequest;
import com.example.SWP.dto.response.ghn.DistrictResponse;
import com.example.SWP.dto.response.ghn.ProvinceResponse;
import com.example.SWP.dto.response.ghn.ServiceResponse;
import com.example.SWP.dto.response.ghn.WardResponse;
import com.example.SWP.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class GhnService {

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

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        List<ProvinceResponse> provinces = new ArrayList<>();
        if (data != null) {
            for (Map<String, Object> item : data) {
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

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        List<DistrictResponse> districts = new ArrayList<>();
        if (data != null) {
            for (Map<String, Object> item : data) {
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

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        List<WardResponse> wards = new ArrayList<>();

        if (data != null) {
            for (Map<String, Object> item : data) {
                WardResponse w = new WardResponse();
                Object codeObj = item.get("WardCode");
                if (codeObj != null) w.setWardCode(codeObj.toString());
                w.setWardName((String) item.get("WardName"));
                wards.add(w);
            }
        }

        return wards;
    }


    public void validateAddressIds(Integer provinceId, Integer districtId, String wardCode) {
        if (provinceId == null || districtId == null || wardCode == null || wardCode.isEmpty()) {
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
                .anyMatch(w -> wardCode.equals(w.getWardCode()));
        if (!wardBelongs) {
            throw new BusinessException("WardCode không thuộc DistrictID", 400);
        }
    }

    public String getFullAddress(String streetAddress, Integer provinceId, Integer districtId, String wardCode) {
        if (provinceId == null || districtId == null || wardCode == null) return "";

        List<ProvinceResponse> provinces = getProvinces();
        String provinceName = provinces.stream()
                .filter(p -> p.getProvinceId() == provinceId)
                .map(ProvinceResponse::getProvinceName)
                .findFirst()
                .orElse("");

        List<DistrictResponse> districts = getDistricts(provinceId);
        String districtName = districts.stream()
                .filter(d -> d.getDistrictId() == districtId)
                .map(DistrictResponse::getDistrictName)
                .findFirst()
                .orElse("");

        List<WardResponse> wards = getWards(districtId);
        String wardName = wards.stream()
                .filter(w -> w.getWardCode().equals(wardCode))
                .map(WardResponse::getWardName)
                .findFirst()
                .orElse("");

        return String.join(", ", streetAddress, wardName, districtName, provinceName);
    }

    public Object calculateShippingFee(GhnShippingFeeRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        headers.set("ShopId", "197714");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("from_district_id", request.getFromDistrictId());
        body.put("to_district_id", request.getToDistrictId());
        body.put("to_ward_code", request.getToWardCode());
        body.put("service_id", 100039);
        body.put("weight", request.getWeight());

        List<Map<String, Object>> itemsList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", request.getItemName());
        item.put("quantity", 1);
        item.put("length", request.getLength());
        item.put("width", request.getWidth());
        item.put("height", request.getHeight());
        item.put("weight", request.getWeight());
        itemsList.add(item);

        body.put("items", itemsList);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Object.class
        );

        return response.getBody();
    }

//    public List<ServiceResponse> getAvailableServices(int fromDistrictId, int toDistrictId) {
//        String url = GHN_URL + "/v2/shipping-order/available-services";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Token", GHN_TOKEN);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("shop_id", 197714);
//        body.put("from_district", fromDistrictId);
//        body.put("to_district", toDistrictId);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url, HttpMethod.POST, request, Map.class
//            );
//
//            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
//
//            List<ServiceResponse> services = new ArrayList<>();
//            for (Map<String, Object> item : data) {
//                ServiceResponse service = ServiceResponse.builder()
//                        .service_id((Integer) item.get("service_id"))
//                        .service_type_id((Integer) item.get("service_type_id"))
//                        .short_name((String) item.get("short_name"))
//                        .build();
//                services.add(service);
//            }
//
//            return services;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Không thể lấy danh sách dịch vụ từ GHN: " + e.getMessage());
//        }
//    }
}
