-- =====================================================
-- Booking Service Database Initialization
-- PostgreSQL
-- =====================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- ENUM types
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'booking_status') THEN
        CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REJECTED');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
        CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
    END IF;
END$$;

-- =====================================================
-- Bookings table
-- =====================================================
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    renter_id UUID NOT NULL,
    car_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status booking_status NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (end_date > start_date)
);

-- =====================================================
-- Payments table
-- =====================================================
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    status payment_status NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- Split Access table (killer feature)
-- =====================================================
CREATE TABLE IF NOT EXISTS split_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PRIMARY_DRIVER', 'CO_DRIVER')),
    UNIQUE(booking_id, user_id)
);

-- =====================================================
-- Indexes
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_bookings_renter_id ON bookings(renter_id);
CREATE INDEX IF NOT EXISTS idx_bookings_car_id ON bookings(car_id);
CREATE INDEX IF NOT EXISTS idx_bookings_owner_id ON bookings(owner_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_split_access_booking_id ON split_access(booking_id);

-- =====================================================
-- Seed data
-- =====================================================
INSERT INTO bookings (id, renter_id, car_id, owner_id, start_date, end_date, status, total_price)
VALUES (
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002',
    NOW() + INTERVAL '2 days',
    NOW() + INTERVAL '5 days',
    'CONFIRMED',
    150.00
);
