-- liquibase formatted sql

-- changeset ricardo.noya:011-add-send-payment-reminders-to-cards
ALTER TABLE core_schema.credit_cards
    ADD COLUMN IF NOT EXISTS send_payment_reminders BOOLEAN DEFAULT TRUE;

-- changeset ricardo.noya:012-create-notifications-table
CREATE TABLE core_schema.notifications
(
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL,
    card_id         UUID,
    type            VARCHAR(50)  NOT NULL CHECK (type IN ('PAYMENT_DUE', 'PAYMENT_REMINDER', 'SYSTEM')),
    title           VARCHAR(255) NOT NULL,
    message         TEXT         NOT NULL,
    is_read         BOOLEAN                  DEFAULT FALSE,
    action_url      VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES auth_schema.users (user_id),
    CONSTRAINT fk_notification_card FOREIGN KEY (card_id) REFERENCES core_schema.credit_cards (card_id)
);

-- changeset ricardo.noya:013-create-notification-preferences-table
CREATE TABLE core_schema.notification_preferences
(
    preference_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                UUID    NOT NULL UNIQUE,
    send_payment_reminders BOOLEAN          DEFAULT TRUE,
    reminder_days_before   INT              DEFAULT 5,
    created_at             TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pref_user FOREIGN KEY (user_id) REFERENCES auth_schema.users (user_id)
);

-- changeset ricardo.noya:014-add-index-notifications
CREATE INDEX idx_notifications_user ON core_schema.notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_card ON core_schema.notifications (card_id);
