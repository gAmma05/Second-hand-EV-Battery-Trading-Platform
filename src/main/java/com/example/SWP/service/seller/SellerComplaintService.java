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

        if (Objects.equals(request.isAccepted(), false) && !request.getResolution().isEmpty()) {
            throw new BusinessException("Bạn không thể từ chối mà lại thêm hướng giải quyết, hãy sử dụng lý do khi từ chối", 400);
        }

        if (Objects.equals(request.isAccepted(), true) && Objects.equals(request.isRequestToAdmin(), true)) {
            throw new BusinessException("Bạn chỉ có thể chọn 1 trong 3 lựa chọn", 400);
        }

        if (Objects.equals(request.isAccepted(), true) && !request.getReason().isEmpty()) {
            throw new BusinessException("Bạn không thể chấp nhận khiếu nại nếu thêm lý do, chỉ có thể thêm khi bạn từ chối đơn hàng", 400);
        }

        if (Objects.equals(complaint.getStatus(), ComplaintStatus.BUYER_REJECTED)) {
            complaint.setStatus(ComplaintStatus.SELLER_REVIEWING);
            complaintRepository.save(complaint);
        }

        String notification = null;

        if (request.isAccepted()) {
            complaintMapper.updateComplaint(request, complaint);
            complaint.setStatus(ComplaintStatus.SELLER_RESOLVED);
            notification = "Người bán đã đưa ra hướng giải quyết cho khiếu nại của bạn. Nội dung: " + complaint.getResolutionNotes() + ".";
        } else {
            if (request.isRequestToAdmin()) {
                complaint.setStatus(ComplaintStatus.ADMIN_REVIEWING);
                notification = "Khiếu nại của bạn đã được đưa đến admin xử lý";
            } else {
                if (request.getReason().isEmpty()) {
                    throw new BusinessException("Không được để trống lý do tại sao từ chối", 400);
                }
                complaint.setStatus(ComplaintStatus.SELLER_REJECTED);
                notification = "Khiếu nại của bạn đã bị người bán từ chối, bạn có thể tiếp tục khiếu nại đến người bán hoặc gửi cho admin xử lí nếu cần";
            }
        }

        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(), "Về khiếu nại của bạn", notification);

    }

    public void requestToAdmin(Authentication authentication, Long contractId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        Complaint complaint = complaintRepository.findById(contractId).orElseThrow(
                () -> new BusinessException("Không tìm thấy khiếu nại", 404)
        );

        if (!Objects.equals(complaint.getOrder().getSeller().getId(), user.getId())) {
            throw new BusinessException("Khiếu nại này không thuộc về bạn", 400);
        }

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.BUYER_REJECTED) &&
                !Objects.equals(complaint.getStatus(), ComplaintStatus.SELLER_REVIEWING)) {
            throw new BusinessException("Không thể gửi yêu cầu đến quản trị viên. Khiếu nại phải ở trạng thái bị từ chối hoặc đang xử lý mới có thể yêu cầu", 400);
        }

        complaint.setStatus(ComplaintStatus.ADMIN_REVIEWING);
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
            complaint.setBuyerName(one.getOrder().getBuyer().getFullName());
            response.add(complaint);
        }
        return response;
    }

}
