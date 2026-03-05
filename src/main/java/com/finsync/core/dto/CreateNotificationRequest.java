package com.finsync.core.dto;

import com.finsync.core.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull UUID userId,
        UUID cardId,
        @NotNull NotificationType type,
        @NotBlank String title,
        @NotBlank String message,
        String actionUrl
) {
}
