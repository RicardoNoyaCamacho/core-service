package com.finsync.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountStatementResponse(
        UUID statementId,
        UUID cardId,
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        LocalDate dueDate,
        BigDecimal totalBalance,
        BigDecimal minPayment,
        BigDecimal bonifiablePayment,
        boolean isPaid
) {
}
