package com.example.SWP.service.admin;

import com.example.SWP.configuration.onlineusertracker.OnlineUserTracker;
import com.example.SWP.dto.response.admin.StatsResponse;
import com.example.SWP.entity.wallet.WalletTransaction;
import com.example.SWP.enums.*;
import com.example.SWP.repository.*;
import com.example.SWP.repository.wallet.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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

    WalletTransactionRepository walletTransactionRepository;

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

        statsResponse.setTotalRevenue(getTransactionStats());

        return statsResponse;
    }

    private BigDecimal getTransactionStats() {
        BigDecimal total = BigDecimal.ZERO;
        List<WalletTransaction> list = walletTransactionRepository.findByTypeOrType(TransactionType.PURCHASE_PRIORITY_PACKAGE, TransactionType.PURCHASE_SELLER_PACKAGE);
        for (WalletTransaction transaction : list) {
            total = total.add(transaction.getAmount());
        }
        return total;
    }
}
