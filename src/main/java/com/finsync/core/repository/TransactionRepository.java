package com.finsync.core.repository;

import com.finsync.core.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByCard_CardIdAndTransactionDateBetween(UUID cardId, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByCard_CardId(UUID cardId, Pageable pageable);
}
