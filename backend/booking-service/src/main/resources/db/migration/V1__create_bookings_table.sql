-- =====================================================
-- Booking Service - Create bookings table
-- Version: V1
-- Based on: Booking.java, BookingStatus.java
-- =====================================================

-- Create ENUM type for booking status (matches BookingStatus.java)
CREATE TYPE booking_status_enum AS ENUM ('CREATED', 'PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

-- Create bookings table (matches Booking.java)
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status booking_status_enum NOT NULL DEFAULT 'CREATED',
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date > start_date)
);

-- Create indexes for performance
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_car_id ON bookings(car_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_date_range ON bookings(start_date, end_date);
CREATE INDEX idx_bookings_created_at ON bookings(created_at);

-- Add table comments
COMMENT ON TABLE bookings IS 'Car booking requests made by renters';
COMMENT ON COLUMN bookings.id IS 'Auto-increment primary key (BIGSERIAL)';
COMMENT ON COLUMN bookings.user_id IS 'Reference to user-service (RENTER id)';
COMMENT ON COLUMN bookings.car_id IS 'Reference to car-service (CAR id)';
COMMENT ON COLUMN bookings.start_date IS 'Rental start timestamp';
COMMENT ON COLUMN bookings.end_date IS 'Rental end timestamp (must be after start_date)';
COMMENT ON COLUMN bookings.status IS 'Booking status: CREATED, PENDING, CONFIRMED, CANCELLED, COMPLETED';
COMMENT ON COLUMN bookings.total_price IS 'Total rental cost calculated by days * price_per_day';
COMMENT ON COLUMN bookings.created_at IS 'Booking creation timestamp (auto-set)';

-- Verify table structure
DO $$
BEGIN
    RAISE NOTICE 'Table "bookings" created successfully with all constraints and indexes';
END $$;
