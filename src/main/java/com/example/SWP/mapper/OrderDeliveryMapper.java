package com.example.SWP.mapper;

import com.example.SWP.dto.response.OrderDeliveryResponse;
import com.example.SWP.entity.OrderDelivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDeliveryMapper {
    @Mapping(target = "orderId", source = "order.id")
    OrderDeliveryResponse toOrderDeliveryResponse(OrderDelivery delivery);
    List<OrderDeliveryResponse> toOrderDeliveryResponseList(List<OrderDelivery> deliveryList);
}
