package com.finsync.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentPlanResponse(
        UUID planId,
        UUID cardId,
        String description,
        BigDecimal totalAmount,
        Integer totalInstallments,
        Integer paidInstallments,
        Integer remainingInstallments,
        BigDecimal monthlyPayment,
        BigDecimal remainingDebt,
        LocalDate originalPurchaseDate,
        Boolean isActive
) {
}
