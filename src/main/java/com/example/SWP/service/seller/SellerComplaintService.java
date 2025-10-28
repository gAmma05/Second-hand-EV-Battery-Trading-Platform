package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.ComplaintResolutionRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.mapper.ComplaintMapper;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.example.SWP.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerComplaintService {

    UserRepository userRepository;

    ComplaintMapper complaintMapper;

    ComplaintRepository complaintRepository;

    NotificationService notificationService;

    public void acceptComplaint(Authentication authentication, Long complaintId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException("Complaint not found", 404));

        if (Objects.equals(complaint.getOrder().getSeller().getId(), user.getId())) {
            throw new BusinessException("This complaint is not your", 400);
        }

        complaint.setStatus(ComplaintStatus.RESOLVING);
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(), "About your complaint", "Your complaint has been accepted by the seller. Please wait for the resolution.");
    }

    public void responseComplaint(Authentication authentication, ComplaintResolutionRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        Complaint complaint = complaintRepository.findById(request.getId()).orElseThrow(
                () -> new BusinessException("Complaint not found", 404)
        );

        if (Objects.equals(complaint.getOrder().getSeller().getId(), user.getId())) {
            throw new BusinessException("This complaint is not your", 400);
        }

        complaintMapper.updateComplaint(request, complaint);
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(), "About your complaint", "The seller has given you a resolution for your complaint. Resolution: " + complaint.getResolutionNotes() + ".");

    }

    public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        List<Complaint> list = complaintRepository.findByOrder_Seller_Id(user.getId());
        return getComplaintsList(list);
    }

    private List<ComplaintResponse> getComplaintsList(List<Complaint> list) {
        List<ComplaintResponse> response = new ArrayList<>();
        for (Complaint one : list) {
            ComplaintResponse complaint = complaintMapper.toComplaintResponse(one);
            response.add(complaint);
        }
        return response;
    }

}
