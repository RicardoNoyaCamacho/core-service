package com.finsync.core.controller;

import com.finsync.core.model.CreditCard;
import com.finsync.core.model.Transaction;
import com.finsync.core.repository.CreditCardRepository;
import com.finsync.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CreditCardRepository creditCardRepository;
    private final TransactionRepository transactionRepository;

    private final UUID USER_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    @QueryMapping
    public List<CreditCard> myCards() {
        return creditCardRepository.findByUserId(USER_ID);
    }

    @QueryMapping
    public CreditCard cardDetails(@Argument UUID cardId) {
        return creditCardRepository.findById(cardId)
                .orElseThrow(()-> new RuntimeException("Card Not Found"));
    }

    @SchemaMapping(typeName = "CreditCard")
    public BigDecimal availableCredit(CreditCard card) {
        return card.getCreditLimit().subtract(card.getCurrentBalance());
    }

    @SchemaMapping(typeName = "CreditCard")
    public List<Transaction> transactions(CreditCard card, @Argument int limit) {
        return transactionRepository.findByCard_CardId(
                card.getCardId(),
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "transactionDate"))
        );
    }

}
