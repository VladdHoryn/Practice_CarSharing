-- =====================================================
-- Booking Service - Create bookings table
-- Version: V1
-- =====================================================

CREATE TYPE booking_status_enum AS ENUM ('CREATED', 'PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status booking_status_enum NOT NULL DEFAULT 'CREATED',
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price > 0),
    cancel_deadline TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_dates CHECK (end_date > start_date AND cancel_deadline < start_date)
);
