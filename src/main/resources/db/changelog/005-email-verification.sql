-- liquibase formatted sql

-- changeset ricardo.noya:015-add-email-verified-to-users
ALTER TABLE auth_schema.users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;

-- changeset ricardo.noya:016-create-email-verification-tokens-table
CREATE TABLE auth_schema.email_verification_tokens
(
    token_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL UNIQUE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES auth_schema.users (user_id)
);

-- changeset ricardo.noya:017-create-sent-email-log-table
CREATE TABLE core_schema.sent_email_log
(
    log_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL,
    statement_id UUID        NOT NULL,
    email_type   VARCHAR(50) NOT NULL,
    sent_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sent_email_log_user FOREIGN KEY (user_id) REFERENCES auth_schema.users (user_id),
    CONSTRAINT fk_sent_email_log_statement FOREIGN KEY (statement_id) REFERENCES core_schema.account_statements (statement_id),
    CONSTRAINT uq_sent_email_log UNIQUE (user_id, statement_id, email_type)
);

CREATE INDEX idx_sent_email_log_user_statement ON core_schema.sent_email_log (user_id, statement_id);
