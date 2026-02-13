package com.finsync.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardResponse(
        UUID cardId,
        String alias,
        String last4Digits,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        Integer cutoffDay
) {
}
