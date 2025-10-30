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

import java.time.LocalDateTime;
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

        if (!Objects.equals(complaint.getOrder().getSeller().getId(), user.getId())) {
            throw new BusinessException("This complaint is not your", 400);
        }

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.PENDING)) {
            throw new BusinessException("Failed to accept complaint, complaint is not pending", 400);
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

        if (!Objects.equals(complaint.getOrder().getSeller().getId(), user.getId())) {
            throw new BusinessException("This complaint is not your", 400);
        }

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.RESOLVING)) {
            throw new BusinessException("Failed to response complaint, complaint is not being resolved", 400);
        }

        complaintMapper.updateComplaint(request, complaint);
        complaint.setStatus(ComplaintStatus.RESOLUTION_GIVEN);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(), "About your complaint", "The seller has given you a resolution for your complaint. Resolution: " + complaint.getResolutionNotes() + ".");

    }

    public void requestToAdmin(Authentication authentication, Long contractId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        Complaint complaint = complaintRepository.findById(contractId).orElseThrow(
                () -> new BusinessException("Complaint not found", 404)
        );

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.REJECTED) &&
                !Objects.equals(complaint.getStatus(), ComplaintStatus.RESOLVING)) {
            throw new BusinessException("Failed to request admin, complaint must be rejected or being resolved to request", 400);
        }


        complaint.setStatus(ComplaintStatus.ADMIN_SOLVING);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(), "About your complaint", "The seller has requested admin to solve your complaint. Please wait for the resolution.");
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
