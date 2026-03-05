package com.finsync.core.service;

import com.finsync.core.dto.CreateCardRequest;
import com.finsync.core.dto.CreditCardResponse;
import com.finsync.core.dto.UpdateCardRequest;
import com.finsync.core.model.CreditCard;
import com.finsync.core.repository.CreditCardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;

    @Transactional
    public CreditCardResponse createCard(UUID userId, CreateCardRequest request) {

        CreditCard card = CreditCard.builder()
                .userId(userId)
                .alias(request.alias())
                .last4Digits(request.last4Digits())
                .cutoffDay(request.cutoffDay())
                .creditLimit(request.creditLimit())
                .currentBalance(BigDecimal.ZERO)
                .daysToPay(20)
                .isActive(true)
                .build();

        CreditCard saved = creditCardRepository.save(card);
        return mapToResponse(saved);
    }

    public List<CreditCardResponse> getMyCard(UUID userId) {
        return creditCardRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CreditCardResponse updateCard(UUID cardId, UpdateCardRequest request) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

        if(request.alias() != null) card.setAlias(request.alias());
        if(request.cutoffDay() != null) card.setCutoffDay(request.cutoffDay());
        if(request.creditLimit() != null) card.setCreditLimit(request.creditLimit());

        if(card.getCurrentBalance().compareTo(card.getCreditLimit()) > 0) {
            throw new IllegalArgumentException("El limite nuevo no puede ser menor que el se tenía.");
        }

        return mapToResponse(creditCardRepository.save(card));
    }

    public CreditCard getCardById(UUID cardId) {
        return creditCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada o no te pertenece"));
    }

    private CreditCardResponse mapToResponse(CreditCard card) {
        return new CreditCardResponse(
                card.getCardId(),
                card.getAlias(),
                card.getLast4Digits(),
                card.getCurrentBalance(),
                card.getCreditLimit(),
                card.getCutoffDay()
        );
    }
}
