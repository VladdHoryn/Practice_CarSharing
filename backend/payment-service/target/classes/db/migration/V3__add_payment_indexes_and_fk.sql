-- =====================================================
-- Payment Service - Indexes and triggers
-- Version: V3
-- =====================================================

-- =====================================================
-- 1. INDEXES for performance
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_payments_booking_id
    ON payments(booking_id);

CREATE INDEX IF NOT EXISTS idx_payments_status_date
    ON payments(status, payment_date);

CREATE INDEX IF NOT EXISTS idx_payments_method_status
    ON payments(method, status);

CREATE INDEX IF NOT EXISTS idx_payments_amount
    ON payments(amount);

-- currency analytics (NEW FIELD IN DOMAIN)
CREATE INDEX IF NOT EXISTS idx_payments_currency
    ON payments(currency);

-- idempotency lookup (CRITICAL FOR PAYMENTS)
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_idempotency_key
    ON payments(idempotency_key);

-- provider lookup (useful for reconciliation)
CREATE INDEX IF NOT EXISTS idx_payments_provider_payment_id
    ON payments(provider_payment_id);

-- =====================================================
-- 2. updated_at trigger (FIXED & SAFE)
-- =====================================================

-- IMPORTANT: column must exist in V1 (so no ADD COLUMN here blindly)
-- If you already have V1 with updated_at -> keep only trigger

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

-- =====================================================
-- 3. SAFETY CONSTRAINTS (DOMAIN ALIGNMENT)
-- =====================================================

-- ensure amount is always valid
ALTER TABLE payments
    ALTER COLUMN amount SET NOT NULL;

ALTER TABLE payments
    ADD CONSTRAINT chk_payment_amount_positive
        CHECK (amount > 0);

-- ensure currency exists (DOMAIN REQUIREMENT)
ALTER TABLE payments
    ALTER COLUMN currency SET NOT NULL;
