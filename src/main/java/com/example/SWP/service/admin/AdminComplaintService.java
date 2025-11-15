package com.example.SWP.service.admin;

import com.example.SWP.dto.request.admin.HandleComplaintRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.escrow.Escrow;
import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.EscrowStatus;
import com.example.SWP.enums.ResolutionType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ComplaintMapper;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.repository.escrow.EscrowRepository;
import com.example.SWP.service.escrow.EscrowService;
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

    EscrowRepository escrowRepository;

    EscrowService escrowService;

    public void handleComplaint(HandleComplaintRequest request) {
        Optional<Complaint> complaintOptional = complaintRepository.findById(request.getComplaintId());
        if (complaintOptional.isEmpty()) {
            throw new BusinessException("Không tìm thấy bản khiếu nại", 404);
        }

        Complaint complaint = complaintOptional.get();

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.ADMIN_REVIEWING)) {
            throw new BusinessException("Bạn chưa thể giải quyết bản khiếu nại này vì nó đang ở trạng thái mà chưa cần bạn (Admin) giải can thiệp", 400);
        }

        if (Objects.equals(request.getResolutionType(), ResolutionType.REFUND)) {
            complaint.setStatus(ComplaintStatus.CLOSED_REFUND); //admin dung ve phia buyer
            Optional<Escrow> escrowOptional = escrowRepository.findByOrder_Id(complaint.getOrder().getId());
            if (escrowOptional.isEmpty()) {
                throw new BusinessException("Không tìm thấy escrow từ order, hãy thử lại!", 404);
            }
            Escrow escrow = escrowOptional.get();
            walletService.refundToWallet(complaint.getOrder().getBuyer(), escrow.getTotalAmount());
            escrowService.switchStatus(EscrowStatus.REFUNDED_TO_BUYER, complaint.getOrder().getId());
        } else if (Objects.equals(request.getResolutionType(), ResolutionType.NO_REFUND)) {
            complaint.setStatus(ComplaintStatus.CLOSED_NO_REFUND); //admin dung ve phia seller
        }
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
    }

    public List<ComplaintResponse> getMyComplaints() {
        List<Complaint> list = complaintRepository.findByStatus(ComplaintStatus.ADMIN_REVIEWING);
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
