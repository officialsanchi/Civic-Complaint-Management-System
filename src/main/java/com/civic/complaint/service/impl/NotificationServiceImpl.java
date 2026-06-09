package com.civic.complaint.service.impl;

import com.civic.complaint.dto.response.NotificationResponse;
import com.civic.complaint.exception.ResourceNotFoundException;
import com.civic.complaint.exception.UnauthorizedException;
import com.civic.complaint.model.Notification;
import com.civic.complaint.enums.NotificationType;
import com.civic.complaint.model.User;
import com.civic.complaint.repository.NotificationRepository;
import com.civic.complaint.utile.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl  {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public void sendNotification(User user, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository
                .findByUserOrderBySentAtDesc(user, pageable)
                .map(notificationMapper::toResponse);
    }

    public void markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not your notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(User user) {
        notificationRepository.markAllReadForUser(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }
}
