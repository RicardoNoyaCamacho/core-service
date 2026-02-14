package com.finsync.core.dto;

import java.math.BigDecimal;

public record UpdateCardRequest(
        String alias,
        Integer cutoffDay,
        BigDecimal creditLimit
) {
}
