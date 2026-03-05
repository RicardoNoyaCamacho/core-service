package com.finsync.core.controller;

import com.finsync.core.dto.AccountStatementResponse;
import com.finsync.core.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @GetMapping("/{cardId}")
    public ResponseEntity<List<AccountStatementResponse>> getStatements(@PathVariable UUID cardId) {
        return ResponseEntity.ok(statementService.getStatements(cardId));
    }

    @PostMapping("/{cardId}/cutoff")
    public ResponseEntity<String> generateCutoff(@PathVariable UUID cardId) {
        statementService.generateCutoff(cardId);
        return ResponseEntity.ok("Corte generado exitosamente. Revisa tu estado de cuenta ");
    }
}
