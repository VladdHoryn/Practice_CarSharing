-- =====================================================
-- Booking Service - Create payments table
-- Version: V3
-- =====================================================

-- Create ENUM types for payment
CREATE TYPE payment_method_enum AS ENUM ('CARD', 'CASH', 'ONLINE');
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');

-- Create payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method payment_method_enum NOT NULL DEFAULT 'ONLINE',
    status payment_status_enum NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);

-- Add comments
COMMENT ON TABLE payments IS 'Payments for bookings (one-to-one with bookings)';
COMMENT ON COLUMN payments.booking_id IS 'Reference to bookings table (unique, 1:1 relationship)';
COMMENT ON COLUMN payments.method IS 'Payment method: CARD, CASH, ONLINE';
COMMENT ON COLUMN payments.status IS 'Payment status: PENDING, SUCCESS, FAILED, REFUNDED';
