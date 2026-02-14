package com.finsync.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "installment_plans",schema = "core_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CreditCard card;

    @Column(nullable = false)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    @Column(name = "paid_installments")
    private Integer paidInstallments;

    @Column(name = "original_purchase_date", nullable = false)
    private LocalDate originalPurchaseDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public BigDecimal getRemainingDebt() {
        if(totalInstallments == 0) return BigDecimal.ZERO;
        BigDecimal amountPerMonth = totalAmount.divide(BigDecimal.valueOf(totalInstallments), 2, java.math.BigDecimal.ROUND_HALF_UP);
        int remainingMonths = totalInstallments - paidInstallments;
        return amountPerMonth.multiply(BigDecimal.valueOf(remainingMonths));
    }
}
