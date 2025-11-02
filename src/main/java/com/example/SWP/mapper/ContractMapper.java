package com.example.SWP.mapper;

import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractMapper {
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.buyer.fullName", target = "buyerName")
    @Mapping(source = "order.buyer.address", target = "buyerAddress")
    @Mapping(source = "order.buyer.phone", target = "buyerPhone")
    @Mapping(source = "order.seller.fullName", target = "sellerName")
    @Mapping(source = "order.seller.address", target = "sellerAddress")
    @Mapping(source = "order.seller.phone", target = "sellerPhone")
    @Mapping(source = "order.post.productType", target = "productType")
    @Mapping(source = "order.post.weight", target = "weight")
    @Mapping(source = "order.deliveryMethod", target = "deliveryMethod")
    @Mapping(source = "order.paymentType", target = "paymentType")
    @Mapping(source = "order.post.vehicleBrand", target = "vehicleBrand")
    @Mapping(source = "order.post.model", target = "model")
    @Mapping(source = "order.post.yearOfManufacture", target = "yearOfManufacture")
    @Mapping(source = "order.post.color", target = "color")
    @Mapping(source = "order.post.mileage", target = "mileage")
    @Mapping(source = "order.post.batteryType", target = "batteryType")
    @Mapping(source = "order.post.capacity", target = "capacity")
    @Mapping(source = "order.post.voltage", target = "voltage")
    @Mapping(source = "order.post.batteryBrand", target = "batteryBrand")
    @Mapping(source = "order.post.price", target = "price")
    @Mapping(source = "order.shippingFee", target = "shippingFee")
    @Mapping(source = "order.depositPercentage", target = "depositPercentage")
    ContractResponse toContractResponse(Contract contract);

    List<ContractResponse> toContractResponses(List<Contract> contracts);
}
