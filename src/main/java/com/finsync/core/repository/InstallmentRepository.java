package com.finsync.core.repository;

import com.finsync.core.model.InstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstallmentRepository extends JpaRepository<InstallmentPlan, UUID> {
    List<InstallmentPlan> findByCard_CardId(UUID cardId);
    List<InstallmentPlan> findByCard_CardIdAndIsActiveTrue(UUID cardId);
}
