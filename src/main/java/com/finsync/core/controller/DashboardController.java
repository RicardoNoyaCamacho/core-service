package com.finsync.core.controller;

import com.finsync.core.dto.CategoryStatResponse;
import com.finsync.core.dto.InstallmentPlanResponse;
import com.finsync.core.model.CreditCard;
import com.finsync.core.model.Transaction;
import com.finsync.core.model.User;
import com.finsync.core.service.CreditCardService;
import com.finsync.core.service.StatisticsService;
import com.finsync.core.service.TransactionService;
import lombok.RequiredArgsConstructor;
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

    private final CreditCardService creditCardService;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    @QueryMapping
    public List<CreditCard> myCards(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return creditCardService.getCardEntitiesByUserId(user.getUserId());
    }

    @QueryMapping
    public CreditCard cardDetails(@Argument UUID cardId) {
        return creditCardService.getCardById(cardId);
    }

    @QueryMapping
    public List<CategoryStatResponse> categoryStats(@Argument UUID cardId) {
        return statisticsService.getCategoryStats(cardId);
    }

    @SchemaMapping(typeName = "CreditCard")
    public BigDecimal availableCredit(CreditCard card) {
        return card.getCreditLimit().subtract(card.getCurrentBalance());
    }

    @SchemaMapping(typeName = "CreditCard")
    public List<Transaction> transactions(CreditCard card, @Argument int limit) {
        return transactionService.getTransactionsByCardIdPaged(card.getCardId(), limit);
    }

    @SchemaMapping(typeName = "CreditCard")
    public List<InstallmentPlanResponse> installmentPlans(CreditCard card) {
        return transactionService.getActiveInstallments(card.getCardId());
    }

}
