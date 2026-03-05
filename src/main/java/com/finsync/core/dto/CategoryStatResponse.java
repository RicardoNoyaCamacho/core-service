package com.finsync.core.dto;

import java.math.BigDecimal;

public record CategoryStatResponse(
        String category,
        BigDecimal total,
        long transactionCount
) {
}
