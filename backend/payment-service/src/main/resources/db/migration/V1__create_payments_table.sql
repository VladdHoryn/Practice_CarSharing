-- =====================================================
-- Payment Service - Create payments table
-- Version: V1
-- =====================================================

CREATE TYPE payment_method_enum AS ENUM ('CARD', 'CASH', 'ONLINE');
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method payment_method_enum NOT NULL DEFAULT 'ONLINE',
    status payment_status_enum NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);

CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

COMMENT ON TABLE payments IS 'Payments for bookings (managed by payment-service)';
COMMENT ON COLUMN payments.booking_id IS 'Reference to booking-service booking';
