-- liquibase formatted sql

-- changeset ricardo.noya:001-create-schemas
CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS core_schema;

--changeset ricardo.noya:002-create-users-table
CREATE TABLE auth_schema.users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

--changeset ricardo.noya:003-create-credit-cards-table
CREATE TABLE core_schema.credit_cards (
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    alias VARCHAR(50) NOT NULL,
    last_4_digits VARCHAR(4),
    cutoff_day INT NOT NULL CHECK ( cutoff_day BETWEEN 1 AND 31 ),
    days_to_pay INT NOT NULL  DEFAULT 20,
    credit_limit DECIMAL(15, 2) NOT NULL,
    current_balance DECIMAL(15, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_card FOREIGN KEY (user_id) REFERENCES auth_schema.users(user_id)
);

--changeset ricardo.noya:004-create-transaction-table
CREATE TABLE core_schema.transactions (
  transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  card_id UUID NOT NULL,
  amount DECIMAL(15, 2) NOT NULL,
  description VARCHAR(255) NOT NULL,
  transaction_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  type VARCHAR(20) NOT NULL CHECK ( type IN ('EXPENSE', 'PAYMENT') ),
  category VARCHAR(50),
  status VARCHAR(20) DEFAULT 'PENDING',
  CONSTRAINT fk_card_trx FOREIGN KEY (card_id) REFERENCES core_schema.credit_cards(card_id)
);

--changeset ricardo.noya:005-create-savings-table
CREATE TABLE core_schema.savings_goals (
    goal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_amount DECIMAL(15, 2),
    current_amount DECIMAL(15, 2) DEFAULT 0.00,
    deadline DATE,
    icon_id VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_saving FOREIGN KEY (user_id) REFERENCES auth_schema.users(user_id)
);

--changeset ricardo.noya:006-create-indexes
CREATE INDEX idx_trx_card_date ON core_schema.transactions(card_id, transaction_date DESC );
CREATE INDEX idx_cards_user ON core_schema.credit_cards(user_id);
CREATE INDEX idx_savings_user ON core_schema.savings_goals(user_id);