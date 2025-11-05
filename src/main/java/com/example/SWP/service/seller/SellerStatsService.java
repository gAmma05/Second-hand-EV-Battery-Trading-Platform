package com.example.SWP.service.seller;

import com.example.SWP.entity.User;
import com.example.SWP.enums.OrderStatus;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.OrderRepository;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.service.validate.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SellerStatsService {

    OrderRepository orderRepository;
    ValidateService validateService;
    PostRepository postRepository;

    public Map<String, Long> getOrderCountByStatus(Authentication authentication, Integer year, Integer month) {
        if(year == null && month != null) {
            throw new BusinessException("Tháng phải đi kèm với năm", 400);
        }

        User seller = validateService.validateCurrentUser(authentication);

        Map<String, Long> stats = new HashMap<>();

        if (year != null) {
            LocalDateTime start;
            LocalDateTime end;

            if (month != null) {
                start = LocalDateTime.of(year, month, 1, 0, 0);
                end = start.plusMonths(1);
            } else {
                start = LocalDateTime.of(year, Month.JANUARY, 1, 0, 0);
                end = start.plusYears(1);
            }

            for (OrderStatus status : OrderStatus.values()) {
                long count = orderRepository.countBySellerAndStatusAndCreatedAtBetween(
                        seller, status, start, end
                );
                stats.put(status.name(), count);
            }
        } else {
            for (OrderStatus status : OrderStatus.values()) {
                long count = orderRepository.countBySellerAndStatus(seller, status);
                stats.put(status.name(), count);
            }
        }
        return stats;
    }

    public Map<String, Long> getPostCountByStatus(Authentication authentication, Integer year, Integer month) {
        if(year == null && month != null) {
            throw new BusinessException("Tháng phải đi kèm với năm", 400);
        }

        User seller = validateService.validateCurrentUser(authentication);
        Map<String, Long> stats = new HashMap<>();

        LocalDateTime start;
        LocalDateTime end;

        if (year != null) {
            if (month != null) {
                start = LocalDateTime.of(year, month, 1, 0, 0);
                end = start.plusMonths(1);
            } else {
                start = LocalDateTime.of(year, 1, 1, 0, 0);
                end = start.plusYears(1);
            }
            for (PostStatus status : PostStatus.values()) {
                long count = postRepository.countByUserAndStatusAndPostDateBetween(
                        seller, status, start, end
                );
                stats.put(status.name(), count);
            }
        } else {
            for (PostStatus status : PostStatus.values()) {
                long count = postRepository.countByUserAndStatus(seller, status);
                stats.put(status.name(), count);
            }
        }

        return stats;
    }
}
