package com.finsync.core.dto;

import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID preferenceId,
        boolean sendPaymentReminders,
        int reminderDaysBefore
) {
}
