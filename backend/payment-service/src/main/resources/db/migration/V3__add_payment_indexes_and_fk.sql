CREATE INDEX IF NOT EXISTS idx_payments_booking_id
    ON payments(booking_id);

CREATE INDEX IF NOT EXISTS idx_payments_status_date
    ON payments(status, payment_date);

CREATE INDEX IF NOT EXISTS idx_payments_method_status
    ON payments(method, status);

CREATE INDEX IF NOT EXISTS idx_payments_amount
    ON payments(amount);

CREATE INDEX IF NOT EXISTS idx_payments_currency
    ON payments(currency);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_idempotency_key
    ON payments(idempotency_key);

CREATE INDEX IF NOT EXISTS idx_payments_provider_payment_id
    ON payments(provider_payment_id);

CREATE OR REPLACE FUNCTION update_payment_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_payment_updated_at ON payments;

CREATE TRIGGER trigger_payment_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_updated_at();

ALTER TABLE payments
    ALTER COLUMN amount SET NOT NULL;

ALTER TABLE payments
    ADD CONSTRAINT chk_payment_amount_positive
        CHECK (amount > 0);

ALTER TABLE payments
    ALTER COLUMN currency SET NOT NULL;
