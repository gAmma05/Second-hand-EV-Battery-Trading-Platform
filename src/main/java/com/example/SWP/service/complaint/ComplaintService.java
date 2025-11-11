package com.example.SWP.service.complaint;

import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ComplaintMapper;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComplaintService {

    UserRepository userRepository;

    ComplaintRepository complaintRepository;

    ComplaintMapper complaintMapper;

    public ComplaintResponse getComplaintDetail(Authentication authentication, Long complaintId) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        Complaint complaint = null;

        if (Objects.equals(user.getRole(), Role.SELLER)) {
            complaint = complaintRepository.findByIdAndOrder_Seller_Id(complaintId, user.getId());
        } else if (Objects.equals(user.getRole(), Role.BUYER)) {
            complaint = complaintRepository.findByIdAndOrder_Buyer_Id(complaintId, user.getId());
        } else if (Objects.equals(user.getRole(), Role.ADMIN)) {
            complaint = complaintRepository.findById(complaintId).orElseThrow(
                    () -> new BusinessException("Không tìm thấy bản hợp đồng", 404)
            );
        }

        if (complaint.getOrder() == null) {
            throw new BusinessException("Không tìm thấy order", 404);
        }

        ComplaintResponse response = complaintMapper.toComplaintResponse(complaint);
        if (user.getRole() == Role.SELLER) {
            response.setBuyerName(complaint.getOrder().getBuyer().getFullName());
        } else if (user.getRole() == Role.BUYER) {
            response.setSellerName(complaint.getOrder().getSeller().getFullName());
        } else if (user.getRole() == Role.ADMIN) {
            response.setSellerName(complaint.getOrder().getSeller().getFullName());
            response.setBuyerName(complaint.getOrder().getBuyer().getFullName());
        }
        return response;
    }
}
