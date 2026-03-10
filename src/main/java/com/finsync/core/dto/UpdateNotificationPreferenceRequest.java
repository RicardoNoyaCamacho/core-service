package com.finsync.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateNotificationPreferenceRequest(
        Boolean sendPaymentReminders,
        @Min(1) @Max(15) Integer reminderDaysBefore
) {
}
