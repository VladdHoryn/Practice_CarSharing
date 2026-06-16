-- =====================================================
-- Booking Service - Create booking_drivers table (Split Access)
-- Version: V5
-- =====================================================

CREATE TYPE booking_driver_status_enum AS ENUM (
    'PENDING',
    'ACCEPTED',
    'DECLINED'
);

CREATE TABLE booking_drivers (
                               id BIGSERIAL PRIMARY KEY,
                               booking_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               email VARCHAR(255) NOT NULL,
                               driver_code VARCHAR(10) NOT NULL,
                               status booking_driver_status_enum NOT NULL DEFAULT 'PENDING',
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION check_max_drivers()
RETURNS TRIGGER AS $$
DECLARE
driver_count INTEGER;
BEGIN
SELECT COUNT(*) INTO driver_count FROM booking_drivers WHERE booking_id = NEW.booking_id;
IF driver_count >= 2 THEN
        RAISE EXCEPTION 'Maximum 2 additional drivers allowed per booking';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_max_drivers
  BEFORE INSERT ON booking_drivers
  FOR EACH ROW
  EXECUTE FUNCTION check_max_drivers();

-- Індекси
CREATE INDEX idx_booking_drivers_booking ON booking_drivers(booking_id);
CREATE INDEX idx_booking_drivers_user ON booking_drivers(user_id);
CREATE INDEX idx_booking_drivers_driver_code ON booking_drivers(driver_code);
CREATE INDEX idx_booking_drivers_email ON booking_drivers(email);
CREATE INDEX idx_booking_drivers_status ON booking_drivers(status);
