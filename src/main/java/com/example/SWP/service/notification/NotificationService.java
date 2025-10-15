package com.example.SWP.service.notification;

import com.example.SWP.entity.notification.Notification;
import com.example.SWP.entity.User;
import com.example.SWP.entity.notification.UserNotification;
import com.example.SWP.entity.notification.UserNotificationKey;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.repository.NotificationRepository;
import com.example.SWP.repository.UserNotificationRepository;
import com.example.SWP.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    UserRepository userRepository;

    NotificationRepository notificationRepository;

    UserNotificationRepository userNotificationRepository;

    public NotificationService(UserRepository userRepository, NotificationRepository notificationRepository,
                               UserNotificationRepository userNotificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
    }

    public void sendNotificationToOneUser(Long id, String title, String content) {
        if (title == null || content == null) {
            return;
        }

        if (!userRepository.existsById(id)) {
            throw new BusinessException("User does not exist", 404);
        }

        User user = userRepository.getReferenceById(id);

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());

        createNotification(notification);

        UserNotification userNotification = new UserNotification();
        userNotification.setUser(user);
        userNotification.setNotification(notification);

        createUserNotification(userNotification);
    }

    private void createNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    private void createUserNotification(UserNotification userNotification){
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
            UserNotification userNotification = new UserNotification();
            userNotification.setUser(user);
            userNotification.setNotification(notification);

            createUserNotification(userNotification);
        }
    }

    public void markAsRead(Long userId, Long notificationId){
        UserNotificationKey key = new UserNotificationKey(userId, notificationId);
        UserNotification userNotification = userNotificationRepository.findById(key)
                .orElseThrow(() -> new BusinessException("Not found", 404));

        userNotification.setRead(true);
        userNotificationRepository.save(userNotification);
    }

}
