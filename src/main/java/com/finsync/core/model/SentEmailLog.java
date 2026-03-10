package com.finsync.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sent_email_log", schema = "core_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentEmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID logId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "statement_id", nullable = false)
    private UUID statementId;

    @Column(name = "email_type", nullable = false, length = 50)
    private String emailType;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;
}
