package com.finsync.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateCardRequest(
        String alias,
        Integer cutoffDay,
        BigDecimal creditLimit,
        @Min(10) @Max(40) Integer daysToPay
) {
}
