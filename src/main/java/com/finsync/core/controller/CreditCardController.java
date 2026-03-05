package com.finsync.core.controller;

import com.finsync.core.dto.CreateCardRequest;
import com.finsync.core.dto.CreditCardResponse;
import com.finsync.core.dto.UpdateCardRequest;
import com.finsync.core.model.User;
import com.finsync.core.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    @PostMapping
    public ResponseEntity<CreditCardResponse> createCard(
            Authentication authentication,
            @Valid @RequestBody CreateCardRequest createCardRequest) {
        User user = (User) authentication.getPrincipal();
        CreditCardResponse response = creditCardService.createCard(user.getUserId(), createCardRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> getMyCards(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(creditCardService.getMyCard(user.getUserId()));
    }

    @PatchMapping("/{cardId}")
    public ResponseEntity<CreditCardResponse> updateCard(
            @PathVariable UUID cardId,
            @Valid @RequestBody UpdateCardRequest updateCardRequest
    ) {
        return ResponseEntity.ok(creditCardService.updateCard(cardId, updateCardRequest));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CreditCardResponse> getCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(creditCardService.getCardResponseById(cardId));
    }
}
