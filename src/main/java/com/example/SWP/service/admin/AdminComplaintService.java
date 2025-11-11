package com.example.SWP.service.admin;

import com.example.SWP.dto.request.admin.HandleComplaintRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ComplaintMapper;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AdminComplaintService {

    ComplaintRepository complaintRepository;

    ComplaintMapper complaintMapper;

    WalletService walletService;

    NotificationService notificationService;

    public void handleComplaint(HandleComplaintRequest request) {
        Optional<Complaint> complaintOptional = complaintRepository.findById(request.getComplaintId());
        if (complaintOptional.isEmpty()) {
            throw new BusinessException("Không tìm thấy bản khiếu nại", 404);
        }

        Complaint complaint = complaintOptional.get();

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.ADMIN_SOLVING)) {
            throw new BusinessException("Bạn chưa thể giải quyết bản khiếu nại này vì nó đang ở trạng thái mà chưa cần bạn (Admin) giải can thiệp", 400);
        }

        complaint.setResolutionNotes(request.getResolution());
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setStatus(ComplaintStatus.ADMIN_RESOLUTION_GIVEN);
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(),
                "Về khiếu nại của bạn",
                "Admin đã đề xuất cách xử lí cho khiếu nại của bạn, hãy kiểm tra");

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(),
                "Về khiếu nại trên đơn hàng của bạn",
                "Admin đã đề xuất cách xử lí cho khiếu nại trên đơn hàng của bạn, hãy kiểm tra");
    }

    public void refundToBuyer(Long complaintId) {
        Optional<Complaint> complaintOptional = complaintRepository.findById(complaintId);
        if (complaintOptional.isEmpty()) {
            throw new BusinessException("Không tìm thấy bản khiếu nại", 404);
        }

        Complaint complaint = complaintOptional.get();

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.ADMIN_SOLVING)) {
            throw new BusinessException("Bạn chưa thể giải quyết bản khiếu nại này vì nó đang ở trạng thái mà chưa cần bạn (Admin) giải can thiệp", 400);
        }

        complaint.setStatus(ComplaintStatus.REJECTED);
        complaintRepository.save(complaint);

        walletService.refundToWallet(complaint.getOrder().getBuyer(), complaint.getOrder().getPost().getPrice());

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(),
                "Về khiếu nại của bạn",
                "Khiếu nại của bạn đã hoàn tất. Kết quả: Đơn hàng của bạn sẽ được hoàn tiền lại");

        notificationService.sendNotificationToOneUser(complaint.getOrder().getBuyer().getEmail(),
                "Về khiếu nại trên đơn hàng của bạn",
                "Khiếu nại về đơn hàng của bạn đã hoàn tất. Kết quả: Đơn hàng " + complaint.getOrder().getId() + " của bạn sẽ được hoàn lại và tiền sẽ được hoàn về cho người mua: " + complaint.getOrder().getBuyer().getFullName());

    }

    public List<ComplaintResponse> getMyComplaints() {
        List<Complaint> list = complaintRepository.findByStatus(ComplaintStatus.ADMIN_SOLVING);
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
