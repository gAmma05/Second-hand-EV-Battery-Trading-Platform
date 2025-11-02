package com.example.SWP.service.notification;

import com.example.SWP.dto.response.NotificationResponse;
import com.example.SWP.entity.notification.Notification;
import com.example.SWP.entity.User;
import com.example.SWP.entity.notification.UserNotification;
import com.example.SWP.entity.notification.UserNotificationKey;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.NotificationRepository;
import com.example.SWP.repository.UserNotificationRepository;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    UserRepository userRepository;

    NotificationRepository notificationRepository;

    UserNotificationRepository userNotificationRepository;


    public void sendNotificationToOneUser(String email, String title, String content) {
        if (title == null || content == null) return;
        if (email == null) throw new BusinessException("Email đang bị bỏ trống, vui lòng thử lại", 400);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", 404));

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        UserNotificationKey key = new UserNotificationKey(user.getId(), notification.getId());
        UserNotification userNotification = new UserNotification();
        userNotification.setId(key);
        userNotification.setUser(user);
        userNotification.setNotification(notification);
        userNotification.setRead(false);
        userNotification.setReceivedAt(LocalDateTime.now());

        userNotificationRepository.save(userNotification);
    }


    private void createNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    private void createUserNotification(UserNotification userNotification) {
        userNotificationRepository.save(userNotification);
    }

    public void sendNotificationToAllUsers(String title, String content) {
        if (title == null || content == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());

        createNotification(notification);

        for (User user : userRepository.findAll()) {
            UserNotificationKey key = new UserNotificationKey(user.getId(), notification.getId());
            UserNotification userNotification = new UserNotification();
            userNotification.setId(key);
            userNotification.setUser(user);
            userNotification.setNotification(notification);

            createUserNotification(userNotification);
        }
    }

    public void markAsRead(Authentication authentication, Long notificationId) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException("No user found", 404)
        );

        UserNotificationKey key = new UserNotificationKey(user.getId(), notificationId);
        UserNotification userNotification = userNotificationRepository.findById(key)
                .orElseThrow(() -> new BusinessException("Not found", 404));

        userNotification.setRead(true);
        userNotificationRepository.save(userNotification);
    }

    public List<NotificationResponse> getUnreadNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User does not exist", 404));

        return getNotificationResponseList(userNotificationRepository.findAllByUserAndIsRead(user, false));

    }

    private List<NotificationResponse> getNotificationResponseList(List<UserNotification> notificationsList) {
        List<NotificationResponse> notificationResponseList = new ArrayList<>();
        for(UserNotification one : notificationsList) {
            NotificationResponse response = new NotificationResponse();
            response.setId(one.getNotification().getId());
            response.setTitle(one.getNotification().getTitle());
            response.setContent(one.getNotification().getContent());
            response.setCreatedAt(one.getNotification().getCreatedAt());
            notificationResponseList.add(response);
        }

        return notificationResponseList;
    }

}
