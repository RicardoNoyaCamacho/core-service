package com.finsync.core.service;

import com.finsync.core.dto.CreateInstallmentRequest;
import com.finsync.core.dto.CreateTransactionRequest;
import com.finsync.core.dto.InstallmentPlanResponse;
import com.finsync.core.model.CreditCard;
import com.finsync.core.model.InstallmentPlan;
import com.finsync.core.model.Transaction;
import com.finsync.core.model.TransactionType;
import com.finsync.core.repository.CreditCardRepository;
import com.finsync.core.repository.InstallmentRepository;
import com.finsync.core.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CreditCardRepository creditCardRepository;
    private final InstallmentRepository installmentRepository;

    @Transactional
    public UUID createTransaction(UUID userId, CreateTransactionRequest request) {

        CreditCard card = creditCardRepository.findById(request.cardId())
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

        validateCardOwnership(card, userId);

        Transaction transaction = Transaction.builder()
                .card(card)
                .amount(request.amount())
                .description(request.description())
                .type(request.type())
                .category(request.category())
                .transactionDate(LocalDateTime.now())
                .build();
        if(request.type() == TransactionType.EXPENSE) {
            //Gasto, aumenta la deuda
            BigDecimal newBalance = card.getCurrentBalance().add(request.amount());

            if(newBalance.compareTo(card.getCreditLimit()) > 0) {
                throw new IllegalArgumentException("La transacción excede el límite de la tarjeta");
            }

            card.setCurrentBalance(newBalance);

        } else if(request.type() == TransactionType.PAYMENT) {
            //Pago, disminuye la deuda
            card.setCurrentBalance(card.getCurrentBalance().subtract(request.amount()));
        }

        transactionRepository.save(transaction);
        creditCardRepository.save(card);

        return transaction.getTransactionId();
    }

    @Transactional
    public void addExistingInstallmentPlan(CreateInstallmentRequest request) {
        CreditCard card = creditCardRepository.findById(request.cardId())
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

        if(request.totalAmount().compareTo(card.getCreditLimit()) > 0) {
            throw new IllegalArgumentException("La transacción excede el límite de la tarjeta");
        }

        InstallmentPlan plan = InstallmentPlan.builder()
                .card(card)
                .description(request.description())
                .totalAmount(request.totalAmount())
                .totalInstallments(request.totalInstallments())
                .paidInstallments(request.paidInstallments())
                .originalPurchaseDate(request.originalDate())
                .isActive(true)
                .build();

        installmentRepository.save(plan);

        BigDecimal monthlyPayment = request.totalAmount()
                .divide(BigDecimal.valueOf(request.totalInstallments()), 2, RoundingMode.HALF_EVEN);

        int remainingMonths = request.totalInstallments() - request.paidInstallments();
        BigDecimal remainingDebt = monthlyPayment.multiply(BigDecimal.valueOf(remainingMonths));

        card.setCurrentBalance(card.getCurrentBalance().add(remainingDebt));
        creditCardRepository.save(card);

        Transaction transaction = Transaction.builder()
                .card(card)
                .amount(request.totalAmount())
                .description(request.description() + " (" + request.totalInstallments() + " MSI)")
                .type(TransactionType.EXPENSE)
                .category("Meses Sin Intereses")
                .transactionDate(request.originalDate().atStartOfDay())
                .status("DIFERIDO")
                .build();

        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByCardId(UUID cardId) {
        if (!creditCardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Tarjeta no encontrada");
        }
        return transactionRepository.findByCard_CardIdOrderByTransactionDateDesc(cardId);
    }

    public List<Transaction> getTransactionsByCardIdPaged(UUID cardId, int limit) {
        return transactionRepository.findByCard_CardId(
                cardId,
                org.springframework.data.domain.PageRequest.of(0, limit,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "transactionDate"))
        );
    }

    public List<InstallmentPlanResponse> getActiveInstallments(UUID cardId) {
        if (!creditCardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Tarjeta no encontrada");
        }
        return installmentRepository.findByCard_CardIdAndIsActiveTrue(cardId).stream()
                .map(this::mapToInstallmentResponse)
                .toList();
    }

    private InstallmentPlanResponse mapToInstallmentResponse(InstallmentPlan plan) {
        BigDecimal monthlyPayment = plan.getTotalAmount()
                .divide(BigDecimal.valueOf(plan.getTotalInstallments()), 2, RoundingMode.HALF_UP);
        int remaining = plan.getTotalInstallments() - plan.getPaidInstallments();
        return new InstallmentPlanResponse(
                plan.getPlanId(),
                plan.getCard().getCardId(),
                plan.getDescription(),
                plan.getTotalAmount(),
                plan.getTotalInstallments(),
                plan.getPaidInstallments(),
                remaining,
                monthlyPayment,
                plan.getRemainingDebt(),
                plan.getOriginalPurchaseDate(),
                plan.getIsActive()
        );
    }

    public void validateCardOwnership(UUID cardId, UUID userId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));
        validateCardOwnership(card, userId);
    }

    private void validateCardOwnership(CreditCard card, UUID userId) {
        if (!card.getUserId().equals(userId)) {
            throw new SecurityException("No tienes permiso para operar sobre esta tarjeta");
        }
    }
}
