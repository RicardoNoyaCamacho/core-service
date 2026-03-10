package com.finsync.core.controller;

import com.finsync.core.dto.NotificationPreferenceResponse;
import com.finsync.core.dto.UpdateNotificationPreferenceRequest;
import com.finsync.core.model.User;
import com.finsync.core.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences/notifications")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    @GetMapping
    public ResponseEntity<NotificationPreferenceResponse> getMyPreferences(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationPreferenceService.getPreferences(user.getUserId()));
    }

    @PatchMapping
    public ResponseEntity<NotificationPreferenceResponse> updateMyPreferences(
            Authentication authentication,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationPreferenceService.updatePreferences(user.getUserId(), request));
    }
}
