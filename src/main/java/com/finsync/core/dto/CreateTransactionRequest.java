package com.finsync.core.dto;

import com.finsync.core.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull UUID cardId,
        @NotNull@Positive BigDecimal amount,
        @NotNull String description,
        @NotNull TransactionType type,
        String category
        ) {
}
