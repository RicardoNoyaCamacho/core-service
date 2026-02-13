package com.finsync.core.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateCardRequest(
        @NotBlank(message = "El alias es obligatorio")
        String alias,
        @Size(min=4, max = 4, message = "Deben ser los últimos 4 dígitos")
        String last4Digits,
        @NotNull @Min(1) @Max(31)
        Integer cutoffDay,
        @NotNull @Positive
        BigDecimal creditLimit
) {
}
