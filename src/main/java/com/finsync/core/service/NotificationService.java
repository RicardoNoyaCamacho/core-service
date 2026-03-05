package com.finsync.core.service;

import com.finsync.core.dto.CreateNotificationRequest;
import com.finsync.core.dto.NotificationResponse;
import com.finsync.core.model.CreditCard;
import com.finsync.core.model.Notification;
import com.finsync.core.model.NotificationType;
import com.finsync.core.model.User;
import com.finsync.core.repository.CreditCardRepository;
import com.finsync.core.repository.NotificationRepository;
import com.finsync.core.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CreditCardRepository creditCardRepository;

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        CreditCard card = null;
        if (request.cardId() != null) {
            card = creditCardRepository.findById(request.cardId())
                    .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));
        }

        Notification notification = Notification.builder()
                .user(user)
                .card(card)
                .type(request.type())
                .title(request.title())
                .message(request.message())
                .actionUrl(request.actionUrl())
                .build();

        return mapToResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void createNotificationInternal(User user, CreditCard card, NotificationType type,
                                           String title, String message, String actionUrl) {
        Notification notification = Notification.builder()
                .user(user)
                .card(card)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .build();

        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotificationsByUser(UUID userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadNotificationsByUser(UUID userId) {
        return notificationRepository.findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new SecurityException("No tienes permiso para modificar esta notificación");
        }

        notification.setIsRead(true);
        return mapToResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new SecurityException("No tienes permiso para eliminar esta notificación");
        }

        notificationRepository.delete(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getUser().getUserId(),
                notification.getCard() != null ? notification.getCard().getCardId() : null,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getActionUrl(),
                notification.getCreatedAt()
        );
    }
}
