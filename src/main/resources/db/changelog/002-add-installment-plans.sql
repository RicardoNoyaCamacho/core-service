-- changeset ricardo.noya:007-create-installment-plans
CREATE TABLE core_schema.installment_plans (
    plan_id UUID PRIMARY KEY,
    card_id UUID NOT NULL,
    description VARCHAR(255) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    total_installments INT NOT NULL,
    paid_installments INT DEFAULT 0,
    original_purchase_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_plan FOREIGN KEY (card_id) REFERENCES core_schema.credit_cards(card_id)
);

-- changeset ricardo.noya:008-add-index-plans
CREATE INDEX idx_plans_card ON core_schema.installment_plans(card_id);