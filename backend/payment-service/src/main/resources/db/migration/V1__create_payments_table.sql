-- =====================================================
-- Payment Service - Create payments table
-- Version: V1
-- =====================================================

CREATE TYPE payment_method_enum AS ENUM ('CARD', 'GOOGLE_PAY', 'APPLE_PAY');
CREATE TYPE payment_status_enum AS ENUM ('CREATED', 'PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUNDED');

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method payment_method_enum NOT NULL DEFAULT 'ONLINE',
    status payment_status_enum NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
);
