package com.finsync.core.controller;

import com.finsync.core.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @PostMapping("/{cardId}/cutoff")
    public ResponseEntity<String> generateCutoff(@PathVariable UUID cardId) {
        statementService.generateCutoff(cardId);
        return ResponseEntity.ok("Corte generado exitosamente. Revisa tu estado de cuenta ");
    }
}
