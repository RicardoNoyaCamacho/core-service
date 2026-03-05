package com.finsync.core.dto;

import com.finsync.core.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID userId,
        UUID cardId,
        NotificationType type,
        String title,
        String message,
        Boolean isRead,
        String actionUrl,
        LocalDateTime createdAt
) {
}
