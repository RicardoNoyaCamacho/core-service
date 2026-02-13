package com.finsync.core.controller;

import com.finsync.core.dto.CreateCardRequest;
import com.finsync.core.dto.CreditCardResponse;
import com.finsync.core.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    private final UUID HARDCODED_USER_UUID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    @PostMapping
    public ResponseEntity<CreditCardResponse> createCard(@Valid @RequestBody CreateCardRequest createCardRequest) {
        CreditCardResponse response = creditCardService.createCard(HARDCODED_USER_UUID, createCardRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> getMyCards() {
        return ResponseEntity.ok(creditCardService.getMyCard(HARDCODED_USER_UUID));
    }
}
