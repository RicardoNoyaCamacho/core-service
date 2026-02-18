package com.finsync.core.controller;

import com.finsync.core.model.CreditCard;
import com.finsync.core.model.Transaction;
import com.finsync.core.model.User;
import com.finsync.core.repository.CreditCardRepository;
import com.finsync.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CreditCardRepository creditCardRepository;
    private final TransactionRepository transactionRepository;

    @QueryMapping
    public List<CreditCard> myCards(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return creditCardRepository.findByUserId(user.getUserId());
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
