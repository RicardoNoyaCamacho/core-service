package com.finsync.core.controller;

import com.finsync.core.dto.CreateInstallmentRequest;
import com.finsync.core.dto.CreateTransactionRequest;
import com.finsync.core.dto.InstallmentPlanResponse;
import com.finsync.core.model.Transaction;
import com.finsync.core.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCardId(cardId));
    }

    @GetMapping("/installments/card/{cardId}")
    public ResponseEntity<List<InstallmentPlanResponse>> getInstallments(@PathVariable UUID cardId) {
        return ResponseEntity.ok(transactionService.getActiveInstallments(cardId));
    }

    @PostMapping
    public ResponseEntity<UUID> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        UUID transactionId = transactionService.createTransaction(request);

        return ResponseEntity.ok(transactionId);
    }

    @PostMapping("/installments")
    public ResponseEntity<Void> addInstallmentPlan(@RequestBody @Valid CreateInstallmentRequest request) {
        transactionService.addExistingInstallmentPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
