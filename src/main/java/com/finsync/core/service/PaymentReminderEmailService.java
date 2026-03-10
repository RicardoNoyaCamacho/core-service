package com.finsync.core.service;

import com.finsync.core.model.AccountStatement;
import com.finsync.core.model.CreditCard;
import com.finsync.core.model.EmailReminderType;
import com.finsync.core.model.SentEmailLog;
import com.finsync.core.model.User;
import com.finsync.core.repository.AccountStatementRepository;
import com.finsync.core.repository.SentEmailLogRepository;
import com.finsync.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReminderEmailService {

    private final AccountStatementRepository accountStatementRepository;
    private final SentEmailLogRepository sentEmailLogRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    public void processEmailReminders() {
        for (EmailReminderType reminderType : EmailReminderType.values()) {
            LocalDate targetDate = LocalDate.now().plusDays(reminderType.getDaysBeforeDue());
            List<AccountStatement> statements = accountStatementRepository.findByDueDateAndIsPaidFalse(targetDate);

            for (AccountStatement statement : statements) {
                try {
                    processStatement(statement, reminderType);
                } catch (Exception e) {
                    log.error("Error processing email reminder for statement {}: {}", statement.getStatementId(), e.getMessage());
                }
            }
        }
    }

    private void processStatement(AccountStatement statement, EmailReminderType reminderType) {
        CreditCard card = statement.getCard();

        if (!Boolean.TRUE.equals(card.getSendPaymentReminders())) {
            return;
        }

        if (sentEmailLogRepository.existsByUserIdAndStatementIdAndEmailType(
                card.getUserId(), statement.getStatementId(), reminderType.name())) {
            return;
        }

        User user = userRepository.findById(card.getUserId()).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }

        Map<String, Object> variables = buildTemplateVariables(card, statement, reminderType);
        emailService.sendHtmlEmail(user.getEmail(), reminderType.getSubject(), "payment-reminder", variables);

        SentEmailLog log = SentEmailLog.builder()
                .userId(card.getUserId())
                .statementId(statement.getStatementId())
                .emailType(reminderType.name())
                .build();
        sentEmailLogRepository.save(log);

        PaymentReminderEmailService.log.info("Payment reminder email ({}) sent for statement {} to user {}",
                reminderType.name(), statement.getStatementId(), user.getUserId());
    }

    private Map<String, Object> buildTemplateVariables(CreditCard card, AccountStatement statement, EmailReminderType reminderType) {
        NumberFormat mxnFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String urgencyBgColor;
        String urgencyTextColor;
        String urgencyEmoji;
        String urgencyLabel;

        switch (reminderType) {
            case REMINDER_5_DAYS -> {
                urgencyBgColor = "#34d399";
                urgencyTextColor = "#064e3b";
                urgencyEmoji = "✅";
                urgencyLabel = "5 días restantes";
            }
            case REMINDER_3_DAYS -> {
                urgencyBgColor = "#fbbf24";
                urgencyTextColor = "#78350f";
                urgencyEmoji = "⚠️";
                urgencyLabel = "3 días restantes";
            }
            default -> {
                urgencyBgColor = "#f87171";
                urgencyTextColor = "#7f1d1d";
                urgencyEmoji = "🚨";
                urgencyLabel = "¡Último día!";
            }
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("cardAlias", card.getAlias());
        vars.put("last4Digits", card.getLast4Digits() != null ? card.getLast4Digits() : "****");
        vars.put("totalBalance", mxnFormat.format(statement.getTotalBalance()));
        vars.put("minPayment", mxnFormat.format(statement.getMinPayment()));
        vars.put("dueDate", statement.getDueDate().format(dateFormatter));
        vars.put("daysLeft", reminderType.getDaysBeforeDue());
        vars.put("urgencyBgColor", urgencyBgColor);
        vars.put("urgencyTextColor", urgencyTextColor);
        vars.put("urgencyEmoji", urgencyEmoji);
        vars.put("urgencyLabel", urgencyLabel);
        vars.put("appUrl", baseUrl);
        return vars;
    }
}
