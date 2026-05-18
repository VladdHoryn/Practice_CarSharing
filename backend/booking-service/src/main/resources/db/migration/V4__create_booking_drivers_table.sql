-- =====================================================
-- Booking Service - Create booking_drivers table (Split Access)
-- Version: V4
-- =====================================================

-- =====================================================
-- 1. CREATE ENUM TYPE for driver role
-- =====================================================
CREATE TYPE driver_role_enum AS ENUM ('PRIMARY_DRIVER', 'CO_DRIVER');

-- =====================================================
-- 2. CREATE TABLE booking_drivers
-- =====================================================
CREATE TABLE booking_drivers (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    role driver_role_enum NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Запобігаємо дублюванню одного користувача в одному бронюванні
    CONSTRAINT unique_booking_driver UNIQUE (booking_id, user_id)
);

-- =====================================================
-- 3. ADD CONSTRAINT for max 3 drivers per booking
-- =====================================================
CREATE OR REPLACE FUNCTION check_max_drivers_per_booking()
RETURNS TRIGGER AS $$
DECLARE
    driver_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO driver_count
    FROM booking_drivers
    WHERE booking_id = NEW.booking_id;
    
    IF driver_count >= 3 THEN
        RAISE EXCEPTION 'Booking % already has % drivers. Maximum 3 drivers allowed per booking.', 
            NEW.booking_id, driver_count;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_max_drivers
    BEFORE INSERT ON booking_drivers
    FOR EACH ROW
    EXECUTE FUNCTION check_max_drivers_per_booking();

-- =====================================================
-- 4. ADD INDEXES for performance
-- =====================================================
CREATE INDEX idx_booking_drivers_booking_id ON booking_drivers(booking_id);
CREATE INDEX idx_booking_drivers_user_id ON booking_drivers(user_id);
CREATE INDEX idx_booking_drivers_role ON booking_drivers(role);
CREATE INDEX idx_booking_drivers_booking_role ON booking_drivers(booking_id, role);

-- =====================================================
-- 5. AUTO-UPDATE updated_at
-- =====================================================
CREATE OR REPLACE FUNCTION update_booking_drivers_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_booking_drivers_updated_at
    BEFORE UPDATE ON booking_drivers
    FOR EACH ROW
    EXECUTE FUNCTION update_booking_drivers_updated_at();

-- =====================================================
-- 6. ADD TABLE COMMENTS
-- =====================================================
COMMENT ON TABLE booking_drivers IS 'Split access - multiple drivers per booking (max 3 drivers)';
COMMENT ON COLUMN booking_drivers.booking_id IS 'Reference to bookings table';
COMMENT ON COLUMN booking_drivers.user_id IS 'Reference to user-service (driver)';
COMMENT ON COLUMN booking_drivers.role IS 'Driver role: PRIMARY_DRIVER (main responsible) or CO_DRIVER (additional driver)';
COMMENT ON CONSTRAINT unique_booking_driver ON booking_drivers IS 'Prevents duplicate users in the same booking';
