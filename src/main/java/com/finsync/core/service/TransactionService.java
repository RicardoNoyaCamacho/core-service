package com.finsync.core.service;

import com.finsync.core.dto.CreateInstallmentRequest;
import com.finsync.core.dto.CreateTransactionRequest;
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
    public UUID createTransaction(CreateTransactionRequest request) {

        CreditCard card = creditCardRepository.findById(request.cardId())
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

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
                .amount(request.totalAmount()) // Mostramos el monto total de la compra original
                .description(request.description() + " (" + request.totalInstallments() + " MSI)") // Agregamos info extra
                .type(TransactionType.EXPENSE) // Cuenta como un Gasto
                .category("Meses Sin Intereses")
                .transactionDate(request.originalDate() != null ? request.originalDate().atStartOfDay() : LocalDateTime.now())
                .status("DIFERIDO") // Opcional: Un status diferente
                .build();

        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByCardId(UUID cardId) {
        // Validamos que la tarjeta exista (opcional, pero buena práctica)
        if (!creditCardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Tarjeta no encontrada");
        }
        return transactionRepository.findByCard_CardIdOrderByTransactionDateDesc(cardId);
    }

    public List<InstallmentPlan> getActiveInstallments(UUID cardId) {
        return installmentRepository.findByCard_CardIdAndIsActiveTrue(cardId);
    }
}
