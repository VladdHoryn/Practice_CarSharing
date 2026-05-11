-- =====================================================
-- Booking Service - Create booking_drivers table (split access)
-- Version: V3
-- =====================================================

CREATE TYPE driver_role_enum AS ENUM ('PRIMARY_DRIVER', 'CO_DRIVER');

CREATE TABLE booking_drivers (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    role driver_role_enum NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_booking_driver UNIQUE (booking_id, user_id),
    CONSTRAINT max_drivers_per_booking CHECK (
        (SELECT COUNT(*) FROM booking_drivers bd WHERE bd.booking_id = booking_id) <= 3
    )
);
