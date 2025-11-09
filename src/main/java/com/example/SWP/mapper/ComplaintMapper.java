package com.example.SWP.mapper;

import com.example.SWP.dto.request.buyer.CreateComplaintRequest;
import com.example.SWP.dto.request.buyer.RejectComplaintRequest;
import com.example.SWP.dto.request.seller.ComplaintRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.ComplaintImage;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ComplaintMapper {
    @Mapping(source = "order.id", target = "orderId")
    ComplaintResponse toComplaintResponse(Complaint complaint);

    @Mapping(source = "orderId", target = "order.id")
    @Mapping(source = "complaintType", target = "type")
    Complaint toComplaint(CreateComplaintRequest request);

    @Mapping(source = "complaintId", target = "id")
    @Mapping(source = "resolution", target = "resolutionNotes")
    void updateComplaint(ComplaintRequest request, @MappingTarget Complaint complaint);


    @AfterMapping
    default void mapImages(CreateComplaintRequest request, @MappingTarget Complaint complaint) {
        if (request.getComplaintImages() != null && !request.getComplaintImages().isEmpty()) {
            List<ComplaintImage> images = request.getComplaintImages().stream()
                    .map(url -> ComplaintImage.builder()
                            .imageUrl(url)
                            .complaint(complaint)
                            .build())
                    .toList();
            complaint.setComplaintImages(images);
        }
    }
}
