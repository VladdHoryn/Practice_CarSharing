-- =====================================================
-- Booking Service - Create booking_drivers table (split access)
-- Version: V5
-- =====================================================

-- Create driver role enum
CREATE TYPE driver_role_enum AS ENUM ('PRIMARY_DRIVER', 'CO_DRIVER');

-- Create booking_drivers table
CREATE TABLE booking_drivers (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    role driver_role_enum NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_booking_driver UNIQUE (booking_id, user_id),
    CONSTRAINT max_drivers_per_booking CHECK (
        (SELECT COUNT(*) FROM booking_drivers bd WHERE bd.booking_id = booking_id) <= 3
    )
);

-- Create indexes
CREATE INDEX idx_booking_drivers_booking_id ON booking_drivers(booking_id);
CREATE INDEX idx_booking_drivers_user_id ON booking_drivers(user_id);
CREATE INDEX idx_booking_drivers_role ON booking_drivers(role);

-- Add comments
COMMENT ON TABLE booking_drivers IS 'Split access - multiple drivers per booking (max 3 drivers)';
COMMENT ON COLUMN booking_drivers.booking_id IS 'Reference to booking';
COMMENT ON COLUMN booking_drivers.user_id IS 'Reference to user-service (driver)';
COMMENT ON COLUMN booking_drivers.role IS 'Driver role: PRIMARY_DRIVER or CO_DRIVER';
