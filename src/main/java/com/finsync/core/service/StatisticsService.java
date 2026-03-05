package com.finsync.core.service;

import com.finsync.core.dto.CategoryStatResponse;
import com.finsync.core.model.CreditCard;
import com.finsync.core.model.TransactionType;
import com.finsync.core.repository.CreditCardRepository;
import com.finsync.core.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CreditCardRepository creditCardRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Returns the spending breakdown by category for the current billing period of the given card.
     * The current period starts on the card's cutoff day of the previous month (or current month,
     * depending on whether the cutoff day has already passed this month).
     */
    public List<CategoryStatResponse> getCategoryStats(UUID cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

        LocalDateTime[] periodRange = getCurrentPeriodRange(card.getCutoffDay());
        LocalDateTime periodStart = periodRange[0];
        LocalDateTime periodEnd = periodRange[1];

        List<com.finsync.core.model.Transaction> expenses =
                transactionRepository.findByCard_CardIdAndTypeAndTransactionDateBetween(
                        cardId, TransactionType.EXPENSE, periodStart, periodEnd);

        Map<String, List<com.finsync.core.model.Transaction>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(t ->
                        t.getCategory() != null ? t.getCategory() : "Sin categoría"));

        return grouped.entrySet().stream()
                .map(entry -> {
                    BigDecimal total = entry.getValue().stream()
                            .map(com.finsync.core.model.Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CategoryStatResponse(entry.getKey(), total, entry.getValue().size());
                })
                .sorted((a, b) -> b.total().compareTo(a.total()))
                .toList();
    }

    /**
     * Determines the start and end of the current billing period based on the card's cutoff day.
     * If today is on or after the cutoff day, the period started on the cutoff day of this month.
     * Otherwise, the period started on the cutoff day of the previous month.
     */
    private LocalDateTime[] getCurrentPeriodRange(int cutoffDay) {
        LocalDate today = LocalDate.now();
        int day = Math.min(cutoffDay, today.lengthOfMonth());

        LocalDate periodStart;
        LocalDate periodEnd;

        if (today.getDayOfMonth() >= day) {
            periodStart = today.withDayOfMonth(day);
            periodEnd = today;
        } else {
            LocalDate previousMonth = today.minusMonths(1);
            int prevDay = Math.min(cutoffDay, previousMonth.lengthOfMonth());
            periodStart = previousMonth.withDayOfMonth(prevDay);
            periodEnd = today;
        }

        return new LocalDateTime[]{periodStart.atStartOfDay(), periodEnd.atTime(23, 59, 59)};
    }
}
