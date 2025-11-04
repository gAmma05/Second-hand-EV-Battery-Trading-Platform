package com.example.SWP.service.ghn;

import com.example.SWP.dto.request.ghn.ServiceRequest;
import com.example.SWP.dto.request.ghn.FeeRequest;
import com.example.SWP.dto.response.ghn.*;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.DeliveryProvider;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class GhnService {

    final RestTemplate restTemplate;
    final PostRepository postRepository;
    final UserRepository userRepository;

    @Value("${ghn.token}")
    String GHN_TOKEN;
    @Value("${ghn.url}")
    String GHN_URL;
    @Value(("${ghn.shop-id}"))
    Integer GHN_SHOP_ID;

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
        if (provinceId == null || districtId == null || wardCode == null || wardCode.isBlank()) {
            throw new BusinessException("Tỉnh/thành phố, quận/huyện hoặc phường/xã không được để trống", 400);
        }

        List<DistrictResponse> districts;

        try {
            districts = getDistricts(provinceId);
        } catch (Exception e) {
            throw new BusinessException("Không tìm thấy danh sách quận/huyện cho Tỉnh/TP ID: " + provinceId, 400);
        }

        boolean districtBelongs = districts.stream()
                .anyMatch(d -> Objects.equals(d.getDistrictId(), districtId));
        if (!districtBelongs) {
            throw new BusinessException("Quận/huyện không thuộc Tỉnh/TP tương ứng", 400);
        }

        List<WardResponse> wards;
        try {
            wards = getWards(districtId);
        } catch (Exception e) {
            throw new BusinessException("Không tìm thấy danh sách phường/xã cho Quận/Huyện ID: " + districtId, 400);
        }

        boolean wardBelongs = wards.stream()
                .anyMatch(w -> wardCode.equals(w.getWardCode()));
        if (!wardBelongs) {
            throw new BusinessException("Phường/xã không thuộc Quận/Huyện tương ứng", 400);
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

    public FeeResponse calculateShippingFee(FeeRequest request, User buyer) {

        if(buyer == null) {
            throw  new BusinessException("Người mua không tồn tại", 404);
        }

        if (buyer.getRole() != Role.BUYER) {
            throw new BusinessException("Người dùng không phải là người mua", 400);
        }

        if (buyer.getDistrictId() == null || buyer.getWardCode() == null) {
            throw new BusinessException("Người mua chưa cập nhật địa chỉ", 400);
        }

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy bài đăng", 404));

        User seller = post.getUser();
        if (seller == null) {
            throw new BusinessException("Người bán không tồn tại", 404);
        }

        Integer fromDistrictId = seller.getDistrictId();
        Integer toDistrictId = buyer.getDistrictId();
        String toWardCode = buyer.getWardCode();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        headers.set("ShopId", String.valueOf(GHN_SHOP_ID));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("from_district_id", fromDistrictId);
        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("service_type_id", request.getServiceTypeId());
        body.put("weight", post.getWeight());

        if (request.getServiceTypeId() == 5) {
            List<Map<String, Object>> itemsList = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("weight", post.getWeight());
            itemsList.add(item);
            body.put("items", itemsList);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = GHN_URL + "/v2/shipping-order/fee";

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");


        return FeeResponse.builder()
                .total((Integer) data.get("total"))
                .service_fee((Integer) data.get("service_fee"))
                .insurance_fee((Integer) data.get("insurance_fee"))
                .build();
    }


    public List<AvailableServicesResponse> getAvailableServices(
            ServiceRequest serviceRequest,
            Authentication authentication) {

        String email = authentication.getName();

        Post post = postRepository.findById(serviceRequest.getPostId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy bài đăng", 404));

        User seller = post.getUser();
        if (seller == null) {
            throw new BusinessException("Không tìm thấy người bán", 400);
        }

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người mua", 404));

        Integer fromDistrictId = seller.getDistrictId();
        Integer toDistrictId = buyer.getDistrictId();

        if (fromDistrictId == null) {
            throw new BusinessException("Người bán chưa cập nhật địa chỉ", 400);
        }

        if (toDistrictId == null) {
            throw new BusinessException("Người mua chưa cập nhật địa chỉ", 400);
        }


        String url = GHN_URL + "/v2/shipping-order/available-services";

        HttpHeaders headers = new HttpHeaders();

        headers.set("Token", GHN_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("shop_id", GHN_SHOP_ID);
        body.put("from_district", seller.getDistrictId());
        body.put("to_district", buyer.getDistrictId());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseBody.get("data");

            List<AvailableServicesResponse> services = new ArrayList<>();
            for (Map<String, Object> item : dataList) {
                AvailableServicesResponse service = AvailableServicesResponse.builder()
                        .service_id((Integer) item.get("service_id"))
                        .short_name((String) item.get("short_name"))
                        .service_type_id((Integer) item.get("service_type_id"))
                        .build();
                services.add(service);
            }

            AvailableServicesResponse selectedService = null;

            if (post.getWeight() > 20) {
                for (AvailableServicesResponse s : services) {
                    if (s.getService_type_id() == 5) {
                        selectedService = s;
                        break;
                    }
                }
            } else {
                for (AvailableServicesResponse s : services) {
                    if (s.getService_type_id() == 2) {
                        selectedService = s;
                        break;
                    }
                }
            }

            return selectedService != null
                    ? List.of(selectedService)
                    : Collections.emptyList();

        } catch (Exception e) {
            throw new BusinessException(
                    "Không thể lấy danh sách dịch vụ từ GHN: " + e.getMessage(),
                    500
            );
        }
    }

    public DeliveryStatus getOrderStatus(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            throw new BusinessException("Tracking number không được để trống", 400);
        }

        return DeliveryStatus.DELIVERED;
    }

    public void createGhnOrder(OrderDelivery orderDelivery) {
        if (orderDelivery == null) {
            throw new BusinessException("OrderDelivery không tồn tại", 404);
        }

        orderDelivery.setDeliveryProvider(DeliveryProvider.GHN);
        orderDelivery.setDeliveryTrackingNumber(Utils.generateCode("GHN"));
    }

//    public void validateGhnTokenAndShop(String token, Integer shopId) {
//        String url = GHN_URL + "/v2/shop/all";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Token", token);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("offset", 0);
//        body.put("limit", 50);
//        body.put("client_phone", "");
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
//
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                throw new BusinessException("Lỗi khi gọi GHN", response.getStatusCodeValue());
//            }
//
//            Map<String, Object> responseBody = response.getBody();
//            if (responseBody == null || !responseBody.containsKey("data")) {
//                throw new BusinessException("Không nhận được data từ GHN", 500);
//            }
//
//            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
//            List<Map<String, Object>> shops = (List<Map<String, Object>>) data.get("shops");
//
//            if (shops == null || shops.isEmpty()) {
//                throw new BusinessException("Token hợp lệ nhưng client chưa có shop nào", 400);
//            }
//
//            boolean match = shops.stream()
//                    .anyMatch(shop -> shopId.equals(((Number) shop.get("_id")).intValue()));
//
//            if (!match) {
//                throw new BusinessException("ShopId không thuộc token này", 400);
//            }
//
//        } catch (HttpClientErrorException.Unauthorized e) {
//            throw new BusinessException("Token GHN không hợp lệ", 401);
//        } catch (Exception e) {
//            throw new BusinessException("Lỗi khi validate token/shopId GHN: " + e.getMessage(), 500);
//        }
//    }

}
