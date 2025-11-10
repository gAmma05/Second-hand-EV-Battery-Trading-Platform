package com.example.SWP.mapper;

import com.example.SWP.dto.request.buyer.CreateComplaintRequest;
import com.example.SWP.dto.request.buyer.RejectComplaintRequest;
import com.example.SWP.dto.request.seller.ComplaintRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.ComplaintImage;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ComplaintMapper {
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "complaintImages", target = "imageUrls", qualifiedByName = "mapImagesToUrls")
    ComplaintResponse toComplaintResponse(Complaint complaint);

    @Mapping(source = "orderId", target = "order.id")
    @Mapping(source = "complaintType", target = "type")
    @Mapping(target = "complaintImages", ignore = true)
    Complaint toComplaint(CreateComplaintRequest request);

    @Mapping(source = "complaintId", target = "id")
    @Mapping(source = "resolution", target = "resolutionNotes")
    void updateComplaint(ComplaintRequest request, @MappingTarget Complaint complaint);


    @AfterMapping
    default void mapImages(CreateComplaintRequest request, @MappingTarget Complaint complaint) {
        if (request.getComplaintImages() != null) {
            List<ComplaintImage> images = request.getComplaintImages().stream()
                    .map(url -> ComplaintImage.builder()
                            .imageUrl(url)
                            .complaint(complaint)
                            .build())
                    .toList();
            complaint.setComplaintImages(images);
        }
    }

    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(List<ComplaintImage> images) {
        if (images == null) return null;
        return images.stream()
                .map(ComplaintImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
