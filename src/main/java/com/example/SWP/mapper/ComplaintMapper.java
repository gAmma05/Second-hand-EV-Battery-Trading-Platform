package com.example.SWP.mapper;

import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ComplaintMapper {
    ComplaintResponse toComplaintResponse(Complaint complaint);
}
