package com.example.SWP.task;

import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PendingPaymentCleanupTask {

    PostRepository postRepository;
    UserRepository userRepository;

    @NonFinal
    @Value("${vnpay.expire.minutes:15}")
    int expireMinutes;

    // Chạy mỗi 30 phút
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void cleanExpiredPendingPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expireMinutes);

        List<Post> expiredPosts = postRepository.findByStatusAndPostDateBefore(
                PostStatus.PENDING_PAYMENT, cutoff
        );

        for (Post post : expiredPosts) {
            User user = post.getUser();

            // Trả lại slot đăng bài cho user và xoá bài đăng quá hạn
            user.setRemainingPosts(user.getRemainingPosts() + 1);
            userRepository.save(user);

            postRepository.delete(post);

            log.info("Deleted pending post id={} (user={}) do quá hạn {} phút thanh toán.",
                    post.getId(), user.getEmail(), expireMinutes);
        }
    }
}
