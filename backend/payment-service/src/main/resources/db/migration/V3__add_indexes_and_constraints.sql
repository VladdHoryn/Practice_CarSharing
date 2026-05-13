-- =====================================================
-- Payment Service - Add indexes and constraints
-- Version: V3
-- =====================================================

-- =====================================================
-- 1. Foreign Key Constraint
-- =====================================================

-- Add foreign key to booking-service (logical reference)
-- Physical FK not created across services

-- =====================================================
-- 2. Basic Indexes
-- =====================================================

-- Index for booking_id (зв'язок з бронюванням)
CREATE INDEX idx_payments_booking_id ON payments(booking_id);

-- Index for status (платежі за статусом)
CREATE INDEX idx_payments_status ON payments(status);

-- Index for payment_date (звіти за датою)
CREATE INDEX idx_payments_payment_date ON payments(payment_date);

-- =====================================================
-- 3. Composite Indexes
-- =====================================================

-- Composite index for status + payment_date (звіти по статусах за період)
CREATE INDEX idx_payments_status_date ON payments(status, payment_date);

-- Composite index for method + status (аналітика за способом оплати)
CREATE INDEX idx_payments_method_status ON payments(method, status);

-- =====================================================
-- 4. Partial index for failed payments (моніторинг помилок)
-- =====================================================

CREATE INDEX idx_payments_failed_only ON payments(id, payment_date) 
    WHERE status = 'FAILED';

-- =====================================================
-- 5. Check constraints
-- =====================================================

-- Ensure amount is positive (already exists)
-- Add constraint for transaction_id format for online payments
ALTER TABLE payments ADD CONSTRAINT check_transaction_id_when_online 
    CHECK (method != 'ONLINE' OR (method = 'ONLINE' AND transaction_id IS NOT NULL));

-- =====================================================
-- 6. Updated_at trigger
-- =====================================================

CREATE OR REPLACE FUNCTION update_payments_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_payments_updated_at ON payments;
CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_payments_updated_at();

-- =====================================================
-- 7. Comments
-- =====================================================

COMMENT ON INDEX idx_payments_status_date IS 'Optimizes status-based reports by date range';
COMMENT ON INDEX idx_payments_method_status IS 'Optimizes analytics by payment method and status';
COMMENT ON INDEX idx_payments_failed_only IS 'Partial index for monitoring failed payments';
COMMENT ON CONSTRAINT check_transaction_id_when_online ON payments IS 'Ensures online payments have transaction_id';
