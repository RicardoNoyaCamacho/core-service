package com.finsync.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateInstallmentRequest(
        UUID cardId,
        String description,
        BigDecimal totalAmount,
        Integer totalInstallments,
        Integer paidInstallments,
        LocalDate originalDate
) {
}
