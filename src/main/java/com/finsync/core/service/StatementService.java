package com.finsync.core.service;

import com.finsync.core.dto.AccountStatementResponse;
import com.finsync.core.model.*;
import com.finsync.core.repository.AccountStatementRepository;
import com.finsync.core.repository.CreditCardRepository;
import com.finsync.core.repository.InstallmentRepository;
import com.finsync.core.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final CreditCardRepository creditCardRepository;
    private final TransactionRepository transactionRepository;
    private final InstallmentRepository installmentRepository;
    private final AccountStatementRepository accountStatementRepository;

    @Transactional
    public void generateCutoff(UUID cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

        //Calcular fechas
        LocalDate cutoffDate = LocalDate.now();
        LocalDate startDate = cutoffDate.minusMonths(1).plusDays(1);

        //Validar: corte hoy?
        if(accountStatementRepository.existsByCard_CardIdAndPeriodEndDate(cardId, cutoffDate)) {
            throw new IllegalStateException("Ya existe un estado de cuenta para el corte de hoy");
        }

        //Sumar gastos del periodo
        List<Transaction> periodTransactions = transactionRepository.findByCard_CardIdAndTransactionDateBetween(
                cardId,
                startDate.atStartOfDay(),
                cutoffDate.atTime(23, 59, 59)
        );

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalPayments = BigDecimal.ZERO;

        for(Transaction t : periodTransactions) {
            if(t.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(t.getAmount());
            } else if(t.getType() == TransactionType.PAYMENT) {
                totalPayments = totalPayments.add(t.getAmount());
            }
        }

        BigDecimal periodBalance = totalExpenses.subtract(totalPayments);

        //Sumar cuotas de MSI
        List<InstallmentPlan> activePlans = installmentRepository.findByCard_CardId(cardId);
        BigDecimal installmentsTotal = BigDecimal.ZERO;

        for(InstallmentPlan plan : activePlans) {
            if(Boolean.TRUE.equals(plan.getIsActive())) {
                BigDecimal monthlyInstallment = plan.getTotalAmount()
                        .divide(BigDecimal.valueOf(plan.getTotalInstallments()), 2, java.math.RoundingMode.HALF_UP);

                installmentsTotal = installmentsTotal.add(monthlyInstallment);

                //Actualizar progreso del plan
                plan.setPaidInstallments(plan.getPaidInstallments() + 1);
                if(plan.getPaidInstallments() >= plan.getTotalInstallments()) {
                    plan.setIsActive(false); //Termina la deuda
                }

                installmentRepository.save(plan);
            }
        }

        //Calcular totales
        BigDecimal totalPeriodDebt = periodBalance.add(installmentsTotal);

        //Si el saldo es negativo tienes saldo a favor
        if(totalPeriodDebt.compareTo(BigDecimal.ZERO) < 0) {
            totalPeriodDebt = BigDecimal.ZERO;
        }

        // Pago para no generar intereses
        BigDecimal bonifiablePayment = totalPeriodDebt;

        //Pago minimo; Ej: 10% -No siempre es así-
        BigDecimal minPayment = totalPeriodDebt.multiply(new BigDecimal("0.10"));

        //Guardar Estado de Cuenta
        AccountStatement statement = AccountStatement.builder()
                .card(card)
                .periodStartDate(startDate)
                .periodEndDate(cutoffDate)
                .dueDate(cutoffDate.plusDays(card.getDaysToPay()))
                .totalBalance(totalPeriodDebt)
                .bonifiablePayment(bonifiablePayment)
                .minPayment(minPayment)
                .build();

        accountStatementRepository.save(statement);
    }

    public List<AccountStatementResponse> getStatements(UUID cardId) {
        if (!creditCardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Tarjeta no encontrada");
        }
        return accountStatementRepository.findByCard_CardIdOrderByPeriodEndDateDesc(cardId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AccountStatementResponse mapToResponse(AccountStatement statement) {
        return new AccountStatementResponse(
                statement.getStatementId(),
                statement.getCard().getCardId(),
                statement.getPeriodStartDate(),
                statement.getPeriodEndDate(),
                statement.getDueDate(),
                statement.getTotalBalance(),
                statement.getMinPayment(),
                statement.getBonifiablePayment(),
                statement.isPaid()
        );
    }
}
