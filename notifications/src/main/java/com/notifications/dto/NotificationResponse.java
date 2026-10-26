package com.notifications.dto;

import com.notifications.model.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String recipient,
        String message,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
