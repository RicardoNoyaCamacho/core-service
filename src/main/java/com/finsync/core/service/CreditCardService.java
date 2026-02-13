package com.finsync.core.service;

import com.finsync.core.dto.CreateCardRequest;
import com.finsync.core.dto.CreditCardResponse;
import com.finsync.core.model.CreditCard;
import com.finsync.core.repository.CreditCardRepository;
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
