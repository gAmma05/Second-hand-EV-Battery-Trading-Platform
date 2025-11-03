package com.example.SWP.service.admin;

import com.example.SWP.configuration.onlineusertracker.OnlineUserTracker;
import com.example.SWP.dto.response.admin.StatsResponse;
import com.example.SWP.enums.*;
import com.example.SWP.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatsService {

    OrderRepository orderRepository;

    UserRepository userRepository;

    PostRepository postRepository;

    OnlineUserTracker onlineUserTracker;

    ContractRepository contractRepository;

    ComplaintRepository complaintRepository;

    public StatsResponse getStats() {
        StatsResponse statsResponse = new StatsResponse();

        statsResponse.setTotalUser((int) userRepository.count());
        statsResponse.setTotalOnlineUser(onlineUserTracker.countOnline());

        statsResponse.setTotalSeller(userRepository.countUserByRole(Role.SELLER));
        statsResponse.setTotalBuyer(userRepository.countUserByRole(Role.BUYER));

        statsResponse.setTotalPost((int) postRepository.count());
        statsResponse.setTotalPendingPost(postRepository.countPostByStatus(PostStatus.PENDING));
        statsResponse.setTotalPostedPost(postRepository.countPostByStatus(PostStatus.POSTED));
        statsResponse.setTotalDeletedPost(postRepository.countPostByStatus(PostStatus.DELETED));

        statsResponse.setTotalOrder((int) orderRepository.count());
        statsResponse.setTotalPendingOrder(orderRepository.countOrderByStatus(OrderStatus.PENDING));
        statsResponse.setTotalApprovedOrder(orderRepository.countOrderByStatus(OrderStatus.APPROVED));
        statsResponse.setTotalRejectedOrder(orderRepository.countOrderByStatus(OrderStatus.REJECTED));
        statsResponse.setTotalDoneOrder(orderRepository.countOrderByStatus(OrderStatus.DONE));

        statsResponse.setTotalContract((int) contractRepository.count());
        statsResponse.setTotalPendingContract(contractRepository.countContractByStatus(ContractStatus.PENDING));
        statsResponse.setTotalSignedContract(contractRepository.countContractByStatus(ContractStatus.SIGNED));
        statsResponse.setTotalCancelledContract(contractRepository.countContractByStatus(ContractStatus.CANCELLED));

        statsResponse.setTotalComplaint((int) complaintRepository.count());
        statsResponse.setTotalPendingComplaint(complaintRepository.countComplaintByStatus(ComplaintStatus.PENDING));
        statsResponse.setTotalResolvedComplaint(complaintRepository.countComplaintByStatus(ComplaintStatus.RESOLVED));

        return statsResponse;
    }
}
