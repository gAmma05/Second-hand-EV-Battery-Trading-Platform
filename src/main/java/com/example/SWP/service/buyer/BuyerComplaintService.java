package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.CreateComplaintRequest;
import com.example.SWP.dto.request.buyer.RejectComplaintRequest;
import com.example.SWP.dto.response.ComplaintResponse;
import com.example.SWP.entity.Complaint;
import com.example.SWP.entity.ComplaintImage;
import com.example.SWP.entity.OrderDelivery;
import com.example.SWP.entity.User;
import com.example.SWP.entity.escrow.Escrow;
import com.example.SWP.enums.*;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.ComplaintMapper;
import com.example.SWP.repository.ComplaintRepository;
import com.example.SWP.repository.OrderDeliveryRepository;
import com.example.SWP.repository.UserRepository;
import com.example.SWP.repository.escrow.EscrowRepository;
import com.example.SWP.service.escrow.EscrowService;
import com.example.SWP.service.notification.NotificationService;
import com.example.SWP.service.user.WalletService;
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

    WalletService walletService;

    EscrowService escrowService;

    EscrowRepository escrowRepository;

    public int DUE_DATE = 7;

    public void createComplaint(Authentication authentication, CreateComplaintRequest request) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

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

        List<ComplaintImage> imageList = new ArrayList<>();
        for (String url : request.getComplaintImages()) {
            ComplaintImage image = ComplaintImage.builder().complaint(complaint).imageUrl(url).build();
            imageList.add(image);
        }
        complaint.setComplaintImages(imageList);

        complaint.setStatus(ComplaintStatus.SELLER_REVIEWING);
        complaint.setCreatedAt(LocalDateTime.now());

        complaintRepository.save(complaint);

        Optional<Escrow> escrowOptional = escrowRepository.findByOrder_Id(request.getOrderId());
        if (escrowOptional.isPresent()) {
            escrowService.switchStatus(EscrowStatus.DISPUTED, request.getOrderId()); //neu co khieu nai he thong lap tuc chan tien khong cho di ra
        } else {
            throw new BusinessException("Không tìm thấy tiền trữ trong hệ thống, hãy thử lại!!", 404);
        }


        notificationService.sendNotificationToOneUser(orderDelivery.getOrder().getSeller().getEmail(),
                "Về sản phẩm của bạn",
                "Có người mua đã gửi khiếu nại về sản phẩm của bạn. Vui lòng kiểm tra trong ứng dụng.");
    }

    public void continueComplaintIfRejected(Authentication authentication, Long complaintId) {
        Optional<Complaint> complaintOptional = complaintRepository.findById(complaintId);
        if (complaintOptional.isEmpty()) {
            throw new BusinessException("Không tìm thấy khiếu nại để tiếp tục", 404);
        }

        Complaint complaint = complaintOptional.get();
        if (Objects.equals(complaint.getStatus(), ComplaintStatus.SELLER_REJECTED)) {
            if (ChronoUnit.DAYS.between(LocalDateTime.now(), complaint.getCreatedAt()) >= DUE_DATE) {
                throw new BusinessException("Vì đã quá " + DUE_DATE + " ngày kể từ khi tạo", 400);
            }

            complaint.setStatus(ComplaintStatus.SELLER_REVIEWING);
            complaint.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);
        }
    }

    private void checkCurrentComplaint(Long orderId) {
        Optional<Complaint> complaintList = complaintRepository.findByOrder_Id(orderId);
        if (complaintList.isPresent()) {
            throw new BusinessException("Bạn đã gửi khiếu nại cho order này, hãy kiểm tra nó trong danh sách đơn khiếu nại!", 400);
        }
    }

    private boolean checkExistComplaint(Long orderId) {
        Optional<Complaint> complaintList = complaintRepository.findByOrder_Id(orderId);
        if (complaintList.isPresent()) {
            return true;
        }
        return false;
    }

    public void requestToAdmin(Authentication authentication, CreateComplaintRequest request) {
        int DUE_DATE = 7;

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrderId(request.getOrderId());

        if (!Objects.equals(orderDelivery.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("Đơn hàng này không thuộc về bạn", 400);
        }

        if (!Objects.equals(orderDelivery.getStatus(), DeliveryStatus.RECEIVED)) {
            throw new BusinessException("Không thể tạo khiếu nại. Đơn hàng có thể chưa được giao hoặc chưa được xác nhận nhận hàng", 400);
        }

        Complaint complaint = null;

        if (checkExistComplaint(request.getOrderId())) {
            Optional<Complaint> complaintOpt = complaintRepository.findByOrder_Id(request.getOrderId());
            if (complaintOpt.isEmpty()) {
                throw new BusinessException("Không tìm thấy complaint, hãy thử lại", 404);
            }

            complaint = complaintOpt.get();
            if (!Objects.equals(complaint.getStatus(), ComplaintStatus.SELLER_REJECTED)) {
                throw new BusinessException("Bạn hãy đợi kết quả xử lí của seller trước khi tiến hành gửi cho admin", 400);
            }
            complaint.setStatus(ComplaintStatus.ADMIN_REVIEWING);
            complaint.setUpdatedAt(LocalDateTime.now());
        } else {
            throw new BusinessException("Bạn không thể gửi lên admin nếu trước đó chưa gửi cho seller", 400);
        }

        notificationService.sendNotificationToOneUser(orderDelivery.getOrder().getSeller().getEmail(), "Về khiếu nại trên đơn hàng của bạn", "Đơn khiếu nại đã được gửi lên admin để xử lí.");

        complaintRepository.save(complaint);
    }

    public void acceptComplaint(Authentication authentication, Long complaintId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        Complaint complaint = complaintRepository.findById(complaintId).orElseThrow(() -> new BusinessException("Không tìm thấy khiếu nại", 404));

        if (!Objects.equals(complaint.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("Khiếu nại này không thuộc về bạn", 400);
        }

        if (!Objects.equals(complaint.getStatus(), ComplaintStatus.SELLER_RESOLVED)) {
            throw new BusinessException("Bạn không thể chấp nhận hoặc từ chối bài post này", 400);
        }

        complaint.setStatus(ComplaintStatus.CLOSED_NO_REFUND);
        complaint.getOrder().setStatus(OrderStatus.DONE);
        complaint.getOrder().getPost().setStatus(PostStatus.SOLD);
        complaintRepository.save(complaint);

        notificationService.sendNotificationToOneUser(complaint.getOrder().getSeller().getEmail(), "Về sản phẩm của bạn", "Người mua đã chấp nhận hướng giải quyết của bạn.");
    }

    public void rejectComplaint(Authentication authentication, RejectComplaintRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        Complaint complaint = complaintRepository.findById(request.getComplaintId()).orElseThrow(() -> new BusinessException("Không tìm thấy khiếu nại", 404));

        if (!Objects.equals(complaint.getOrder().getBuyer().getId(), user.getId())) {
            throw new BusinessException("Khiếu nại này không thuộc về bạn", 400);
        }

        if (Objects.equals(complaint.getStatus(), ComplaintStatus.SELLER_RESOLVED)) {
            complaint.setStatus(ComplaintStatus.BUYER_REJECTED);
            complaint.setUpdatedAt(LocalDateTime.now());
            notificationService.sendNotificationToOneUser(complaint.getOrder().getSeller().getEmail(), "Về sản phẩm của bạn", "Người mua đã từ chối hướng giải quyết của bạn. Lý do: " + request.getReason() + ".");
        } else {
            throw new BusinessException("Bạn không thể chấp nhận hoặc từ chối bài khiếu nại này này", 400);
        }
        complaintRepository.save(complaint);
    }

    public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        List<Complaint> list = complaintRepository.findByOrder_Buyer_Id(user.getId());
        return getComplaintsList(list);
    }

    public List<ComplaintResponse> getComplaintsByOrderId(Authentication authentication, Long orderId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        List<Complaint> list = complaintRepository.findByOrder_IdAndOrder_Buyer_Id(orderId, user.getId());
        return getComplaintsList(list);
    }

    private List<ComplaintResponse> getComplaintsList(List<Complaint> list) {
        List<ComplaintResponse> response = new ArrayList<>();
        for (Complaint one : list) {
            ComplaintResponse complaint = complaintMapper.toComplaintResponse(one);
            complaint.setSellerName(one.getOrder().getSeller().getFullName());
            response.add(complaint);
        }
        return response;
    }

}
