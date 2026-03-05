package com.finsync.core.repository;

import com.finsync.core.model.AccountStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountStatementRepository extends JpaRepository<AccountStatement, UUID> {
    boolean existsByCard_CardIdAndPeriodEndDate(UUID cardId, LocalDate periodEndDate);
    List<AccountStatement> findByCard_CardIdOrderByPeriodEndDateDesc(UUID cardId);
    List<AccountStatement> findByDueDateAndIsPaidFalse(LocalDate dueDate);
    List<AccountStatement> findByDueDateAndIsPaidFalseAndCard_UserId(LocalDate dueDate, UUID userId);
}
