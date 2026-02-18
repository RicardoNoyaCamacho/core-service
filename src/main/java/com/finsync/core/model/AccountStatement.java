package com.finsync.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_statements", schema = "core_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID statementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CreditCard card;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_balance", nullable = false)
    private BigDecimal totalBalance; //Lo que debes en total

    @Column(name = "min_payment", nullable = false)
    private BigDecimal minPayment; // Pago minimo 5-10%

    @Column(name = "bonifiable_payment", nullable = false)
    private BigDecimal bonifiablePayment; // Pago para no generar intereses

    @Column(name = "is_paid")
    @Builder.Default
    private boolean isPaid = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
