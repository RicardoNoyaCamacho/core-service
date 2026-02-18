package com.finsync.core.service;

import com.finsync.core.model.*;
import com.finsync.core.repository.AccountStatementRepository;
import com.finsync.core.repository.CreditCardRepository;
import com.finsync.core.repository.InstallmentRepository;
import com.finsync.core.repository.TransactionRepository;
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
                .orElseThrow(() -> new RuntimeException("Credit Card Not Found"));

        //Calcular fechas
        LocalDate cutoffDate = LocalDate.now();
        LocalDate startDate = cutoffDate.minusMonths(1).plusDays(1);

        //Validar: corte hoy?
        if(accountStatementRepository.existsByCard_CardIdAndPeriodEndDate(cardId, cutoffDate)) {
            throw new RuntimeException("Account Statement already exists");
        }

        //Sumar gastos del periodo
        List<Transaction> peroiodTransactions = transactionRepository.findByCard_CardIdAndTransactionDateBetween(
                cardId,
                startDate.atStartOfDay(),
                cutoffDate.atTime(23, 59, 59)
        );

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalPayments = BigDecimal.ZERO;

        for(Transaction t : peroiodTransactions) {
            if(t.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(t.getAmount());
            } else if(t.getType() == TransactionType.PAYMENT) {
                totalPayments = totalPayments.add(t.getAmount());
            }
        }

        BigDecimal periodBalance = totalExpenses.subtract(totalPayments);

        //Sumar coutas de MSI
        List<InstallmentPlan> activePlans = installmentRepository.findByCard_CardId(cardId);
        BigDecimal instalmmentsTotal = BigDecimal.ZERO;

        for(InstallmentPlan plan : activePlans) {
            if(Boolean.TRUE.equals(plan.getIsActive())) {
                BigDecimal monthlyInstallment = plan.getTotalAmount()
                        .divide(BigDecimal.valueOf(plan.getTotalInstallments()), 2, java.math.RoundingMode.HALF_UP);

                instalmmentsTotal = instalmmentsTotal.add(monthlyInstallment);

                //Actualizar progreso del plan
                plan.setPaidInstallments(plan.getPaidInstallments() + 1);
                if(plan.getPaidInstallments() >= plan.getTotalInstallments()) {
                    plan.setIsActive(false); //Termina la deuda
                }

                installmentRepository.save(plan);
            }
        }

        //Calcular totales
        BigDecimal totalPeriodDebt = periodBalance.add(instalmmentsTotal);

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
}
