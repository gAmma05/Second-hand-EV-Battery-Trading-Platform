package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.CreateComplaintRequest;
import com.example.SWP.dto.request.buyer.RejectComplaintRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.DeliveryStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ComplaintMapper;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerComplaintService {

    OrderDeliveryRepository orderDeliveryRepository;

    UserRepository userRepository;

    ComplaintMapper complaintMapper;

    ComplaintRepository complaintRepository;

    NotificationService notificationService;

    public void createComplaint(Authentication authentication, CreateComplaintRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrderId(request.getOrderId());

        if (!Objects.equals(orderDelivery.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("This order is not your", 400);
        }

        if (!Objects.equals(orderDelivery.getStatus(), DeliveryStatus.RECEIVED)) {
            throw new BusinessException("Failed to create complaint, could be not yet deliver or receive", 400);
        }

        Complaint complaint = complaintMapper.toComplaint(request);
        complaint.setStatus(ComplaintStatus.PENDING);

        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(orderDelivery.getOrder().getSeller().getEmail(), "About your product", "Look like someone has complained about your product, please check it out in the app");
    }

    public void acceptComplaint(Authentication authentication, Long complaintId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException("Complaint not found", 404));

        if (!Objects.equals(complaint.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("This complaint is not your", 400);
        }

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getSeller().getEmail(), "About your product", "Your resolution has been accepted by the buyer");
    }

    public void rejectComplaint(Authentication authentication, RejectComplaintRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );
        Complaint complaint = complaintRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("Complaint not found", 404));

        if (!Objects.equals(complaint.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("This complaint is not your", 400);
        }

        complaint.setStatus(ComplaintStatus.REJECTED);
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getSeller().getEmail(), "About your product", "Your resolution has been rejected by the buyer. Reason: " + request.getReason() + ".");
    }

    public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        List<Complaint> list = complaintRepository.findByOrder_Buyer_Id(user.getId());
        return getComplaintsList(list);
    }

    private List<ComplaintResponse> getComplaintsList(List<Complaint> list) {
        List<ComplaintResponse> response = new ArrayList<>();
        for(Complaint one : list) {
            ComplaintResponse complaint = complaintMapper.toComplaintResponse(one);
            response.add(complaint);
        }
        return response;
    }

}
