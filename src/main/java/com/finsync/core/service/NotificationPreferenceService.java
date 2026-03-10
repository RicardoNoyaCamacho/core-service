package com.finsync.core.service;

import com.finsync.core.dto.NotificationPreferenceResponse;
import com.finsync.core.dto.UpdateNotificationPreferenceRequest;
import com.finsync.core.model.NotificationPreference;
import com.finsync.core.model.User;
import com.finsync.core.repository.NotificationPreferenceRepository;
import com.finsync.core.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;

    public NotificationPreferenceResponse getPreferences(UUID userId) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByUser_UserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        return mapToResponse(preference);
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(UUID userId, UpdateNotificationPreferenceRequest request) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByUser_UserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        if (request.sendPaymentReminders() != null) {
            preference.setSendPaymentReminders(request.sendPaymentReminders());
        }
        if (request.reminderDaysBefore() != null) {
            preference.setReminderDaysBefore(request.reminderDaysBefore());
        }

        return mapToResponse(notificationPreferenceRepository.save(preference));
    }

    @Transactional
    private NotificationPreference createDefaultPreferences(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        NotificationPreference preference = NotificationPreference.builder()
                .user(user)
                .sendPaymentReminders(true)
                .reminderDaysBefore(5)
                .build();
        return notificationPreferenceRepository.save(preference);
    }

    private NotificationPreferenceResponse mapToResponse(NotificationPreference preference) {
        return new NotificationPreferenceResponse(
                preference.getPreferenceId(),
                preference.getSendPaymentReminders(),
                preference.getReminderDaysBefore()
        );
    }
}
