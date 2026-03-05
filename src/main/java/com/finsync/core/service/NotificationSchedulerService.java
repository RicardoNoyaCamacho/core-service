package com.finsync.core.service;

import com.finsync.core.model.*;
import com.finsync.core.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {

    private final CreditCardRepository creditCardRepository;
    private final AccountStatementRepository accountStatementRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StatementService statementService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void generateAllPendingCutoffs() {
        log.info("Iniciando generación automática de cortes de tarjetas...");
        int cutoffDay = LocalDate.now().getDayOfMonth();

        List<CreditCard> cardsWithCutoffToday = creditCardRepository.findByCutoffDayAndIsActiveTrue(cutoffDay);
        log.info("Se encontraron {} tarjeta(s) con corte hoy (día {})", cardsWithCutoffToday.size(), cutoffDay);

        for (CreditCard card : cardsWithCutoffToday) {
            try {
                statementService.generateCutoff(card.getCardId());
                log.info("Corte generado para tarjeta {}", card.getCardId());

                userRepository.findById(card.getUserId()).ifPresent(user -> {
                    try {
                        notificationService.createNotificationInternal(
                                user,
                                card,
                                NotificationType.SYSTEM,
                                "Corte generado: " + card.getAlias(),
                                "Tu estado de cuenta del periodo actual ha sido generado para la tarjeta " + card.getAlias() + ".",
                                "/cards/" + card.getCardId() + "/statements"
                        );
                    } catch (Exception e) {
                        log.error("Error al crear notificación de corte para tarjeta {}: {}", card.getCardId(), e.getMessage());
                    }
                });
            } catch (IllegalStateException e) {
                log.info("Corte ya existente para tarjeta {}: {}", card.getCardId(), e.getMessage());
            } catch (Exception e) {
                log.error("Error al generar corte para tarjeta {}: {}", card.getCardId(), e.getMessage());
            }
        }

        log.info("Generación automática de cortes finalizada.");
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void checkPaymentReminders() {
        log.info("Verificando recordatorios de pago...");

        List<NotificationPreference> preferences = notificationPreferenceRepository.findAll();

        for (NotificationPreference preference : preferences) {
        if (!Boolean.TRUE.equals(preference.getSendPaymentReminders())) {
                continue;
            }

            int daysBefore = preference.getReminderDaysBefore() != null ? preference.getReminderDaysBefore() : 5;
            LocalDate targetDueDate = LocalDate.now().plusDays(daysBefore);

            try {
                sendReminderNotifications(preference.getUser(), targetDueDate);
            } catch (Exception e) {
                log.error("Error al procesar recordatorios para usuario {}: {}", preference.getUser().getUserId(), e.getMessage());
            }
        }

        log.info("Verificación de recordatorios finalizada.");
    }

    private void sendReminderNotifications(User user, LocalDate targetDueDate) {
        List<AccountStatement> upcomingStatements = accountStatementRepository
                .findByDueDateAndIsPaidFalseAndCard_UserId(targetDueDate, user.getUserId());

        for (AccountStatement statement : upcomingStatements) {
            CreditCard card = statement.getCard();

            if (!Boolean.TRUE.equals(card.getSendPaymentReminders())) {
                continue;
            }

            try {
                notificationService.createNotificationInternal(
                        user,
                        card,
                        NotificationType.PAYMENT_REMINDER,
                        "Recordatorio de pago: " + card.getAlias(),
                        "Tu fecha de pago para la tarjeta " + card.getAlias() + " es el " + targetDueDate + ". Saldo a pagar: $" + statement.getTotalBalance(),
                        "/cards/" + card.getCardId() + "/statements"
                );
                log.info("Recordatorio de pago enviado para tarjeta {} del usuario {}", card.getCardId(), user.getUserId());
            } catch (Exception e) {
                log.error("Error al enviar recordatorio para tarjeta {}: {}", card.getCardId(), e.getMessage());
            }
        }
    }
}
