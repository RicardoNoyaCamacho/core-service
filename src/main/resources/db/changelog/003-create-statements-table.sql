-- changeset ricard.noya:009-create-statements-table
CREATE TABLE core_schema.account_statements
(
    statement_id       UUID PRIMARY KEY,
    card_id            UUID           NOT NULL,
    period_start_date  DATE           NOT NULL,
    period_end_date    DATE           NOT NULL,
    due_date           DATE           NOT NULL,
    total_balance      DECIMAL(15, 2) NOT NULL,
    min_payment        DECIMAL(15, 2) NOT NULL,
    bonifiable_payment DECIMAL(15, 2) NOT NULL,
    is_paid            BOOLEAN                  DEFAULT FALSE,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_statement FOREIGN KEY (card_id) REFERENCES core_schema.credit_cards (card_id)
);

-- changeset ricardo.noya:010-add-index-statement
CREATE INDEX idx_statements_card ON core_schema.account_statements (card_id, period_end_date DESC);