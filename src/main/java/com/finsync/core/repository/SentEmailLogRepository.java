package com.finsync.core.repository;

import com.finsync.core.model.SentEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SentEmailLogRepository extends JpaRepository<SentEmailLog, UUID> {
    boolean existsByUserIdAndStatementIdAndEmailType(UUID userId, UUID statementId, String emailType);
}
