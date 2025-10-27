package com.example.SWP.mapper;

import com.example.SWP.dto.request.buyer.CreateComplaintRequest;
import com.example.SWP.dto.request.buyer.RejectComplaintRequest;
import com.example.SWP.dto.request.seller.ComplaintResolutionRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ComplaintMapper {
    @Mapping(source = "order.id", target = "orderId")
    ComplaintResponse toComplaintResponse(Complaint complaint);

    @Mapping(source = "orderId", target = "order.id")
    Complaint toComplaint(CreateComplaintRequest request);

    void updateComplaint(RejectComplaintRequest request, @MappingTarget Complaint complaint);
    void updateComplaint(ComplaintResolutionRequest request, @MappingTarget Complaint complaint);
}
