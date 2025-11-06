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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        int DUE_DATE = 7;

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrderId(request.getOrderId());

        if (ChronoUnit.DAYS.between(LocalDateTime.now(), orderDelivery.getCreatedAt()) >= DUE_DATE) {
            throw new BusinessException("Bạn không thể tạo khiếu nại sau " + DUE_DATE + " ngày kể từ khi nhận hàng", 400);
        }

        if (!Objects.equals(orderDelivery.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("Đơn hàng này không thuộc về bạn", 400);
        }

        if (!Objects.equals(orderDelivery.getStatus(), DeliveryStatus.RECEIVED)) {
            throw new BusinessException("Không thể tạo khiếu nại. Đơn hàng có thể chưa được giao hoặc chưa được xác nhận nhận hàng", 400);
        }

        checkCurrentComplaint(request.getOrderId());

        Complaint complaint = complaintMapper.toComplaint(request);
        complaint.setStatus(ComplaintStatus.PENDING);
        complaint.setCreatedAt(LocalDateTime.now());

        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(
                orderDelivery.getOrder().getSeller().getEmail(),
                "Về sản phẩm của bạn",
                "Có người mua đã gửi khiếu nại về sản phẩm của bạn. Vui lòng kiểm tra trong ứng dụng."
        );
    }

    private void checkCurrentComplaint(Long orderId) {
        Optional<Complaint> complaintList = complaintRepository.findByOrder_Id(orderId);
        if (complaintList.isPresent()) {
            throw new BusinessException("Bạn đã gửi khiếu nại cho order này, hãy kiểm tra nó trong danh sách đơn khiếu nại!", 400);
        }
    }

    public void acceptComplaint(Authentication authentication, Long complaintId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy khiếu nại", 404));

        if (!Objects.equals(complaint.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("Khiếu nại này không thuộc về bạn", 400);
        }

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(
                complaint.getOrder().getSeller().getEmail(),
                "Về sản phẩm của bạn",
                "Người mua đã chấp nhận hướng giải quyết của bạn."
        );
    }

    public void rejectComplaint(Authentication authentication, RejectComplaintRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy khiếu nại", 404));

        if (!Objects.equals(complaint.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("Khiếu nại này không thuộc về bạn", 400);
        }

        complaint.setStatus(ComplaintStatus.REJECTED);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(
                complaint.getOrder().getSeller().getEmail(),
                "Về sản phẩm của bạn",
                "Người mua đã từ chối hướng giải quyết của bạn. Lý do: " + request.getReason() + "."
        );
    }

    public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("Không tìm thấy người dùng", 404)
        );

        List<Complaint> list = complaintRepository.findByOrder_Buyer_Id(user.getId());
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
