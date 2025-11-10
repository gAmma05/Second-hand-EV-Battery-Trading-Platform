package com.example.SWP.service.seller;

import com.example.SWP.dto.request.seller.ComplaintRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.User;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.mapper.ComplaintMapper;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.WalletService;
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

    WalletService walletService;

    public void responseComplaint(Authentication authentication, ComplaintRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        Complaint complaint = complaintRepository.findById(request.getComplaintId()).orElseThrow(
                () -> new BusinessException("Không tìm thấy khiếu nại", 404)
        );

        if (!Objects.equals(complaint.getOrder().getSeller().getId(), user.getId())) {
            throw new BusinessException("Khiếu nại này không thuộc về bạn", 400);
        }

        if (Objects.equals(complaint.getStatus(), ComplaintStatus.REJECTED)) {
            complaint.setStatus(ComplaintStatus.PENDING);
            complaintRepository.save(complaint);
        }

        if (request.isAccepted()) {
            complaintMapper.updateComplaint(request, complaint);
            complaint.setStatus(ComplaintStatus.RESOLUTION_GIVEN);
            complaint.setUpdatedAt(LocalDateTime.now());
        } else {
            if (request.isRequestToAdmin()) {
                complaint.setStatus(ComplaintStatus.ADMIN_SOLVING);
            } else {
                complaint.setStatus(ComplaintStatus.REJECTED);
                walletService.refundToWallet(complaint.getOrder().getBuyer(), complaint.getOrder().getPost().getPrice());
            }
        }
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(
                complaint.getOrder().getBuyer().getEmail(),
                "Về khiếu nại của bạn",
                "Người bán đã đưa ra hướng giải quyết cho khiếu nại của bạn. Nội dung: " + complaint.getResolutionNotes() + "."
        );
    }

    public void requestToAdmin(Authentication authentication, Long contractId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        Complaint complaint = complaintRepository.findById(contractId).orElseThrow(
                () -> new BusinessException("Không tìm thấy khiếu nại", 404)
        );

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.REJECTED) &&
                !Objects.equals(complaint.getStatus(), ComplaintStatus.PENDING)) {
            throw new BusinessException("Không thể gửi yêu cầu đến quản trị viên. Khiếu nại phải ở trạng thái bị từ chối hoặc đang xử lý mới có thể yêu cầu", 400);
        }

        complaint.setStatus(ComplaintStatus.ADMIN_SOLVING);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(
                complaint.getOrder().getBuyer().getEmail(),
                "Về khiếu nại của bạn",
                "Người bán đã yêu cầu quản trị viên can thiệp để giải quyết khiếu nại. Vui lòng chờ phản hồi."
        );
    }

    public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        List<Complaint> list = complaintRepository.findByOrder_Seller_Id(user.getId());
        return getComplaintsList(list);
    }

    public List<ComplaintResponse> getComplaintsByOrderId(Authentication authentication, Long orderId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        List<Complaint> list = complaintRepository.findByOrder_IdAndOrder_Seller_Id(orderId, user.getId());
        return getComplaintsList(list);
    }

    private List<ComplaintResponse> getComplaintsList(List<Complaint> list) {
        List<ComplaintResponse> response = new ArrayList<>();
        for (Complaint one : list) {
            ComplaintResponse complaint = complaintMapper.toComplaintResponse(one);
            complaint.setName(one.getOrder().getBuyer().getFullName());
            response.add(complaint);
        }
        return response;
    }

}
