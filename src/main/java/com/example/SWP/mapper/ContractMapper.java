package com.example.SWP.mapper;

import com.example.SWP.dto.response.user.ContractResponse;
import com.example.SWP.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractMapper {
    @Mapping(source = "order.id", target = "orderId")
    ContractResponse toContractResponse(Contract contract);

    List<ContractResponse> toContractResponses(List<Contract> contracts);
}
