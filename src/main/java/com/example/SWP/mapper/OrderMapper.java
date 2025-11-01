package com.example.SWP.mapper;

import com.example.SWP.dto.response.user.OrderResponse;
import com.example.SWP.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "productType", source = "post.productType")
    @Mapping(target = "vehicleBrand", source = "post.vehicleBrand")
    @Mapping(target = "model", source = "post.model")
    @Mapping(target = "batteryBrand", source = "post.batteryBrand")
    @Mapping(target = "batteryType", source = "post.batteryType")
    @Mapping(target = "price", source = "post.price")

    OrderResponse toOrderResponse(Order order);
}
