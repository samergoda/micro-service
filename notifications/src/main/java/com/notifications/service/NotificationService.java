package com.notifications.service;

import com.notifications.dto.CreateNotificationRequest;
import com.notifications.dto.NotificationEvent;
import com.notifications.entity.Notification;

import java.util.List;

public interface NotificationService {

    List<Notification> findAll(String recipient, Boolean read);

    Notification findById(Long id);

    Notification create(CreateNotificationRequest request);

    Notification createFromEvent(NotificationEvent event);

    Notification markAsRead(Long id);

    void delete(Long id);
}
