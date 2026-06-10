-- =====================================================
-- Booking Service - Create booking_drivers table (Split Access)
-- Version: V5
-- =====================================================

-- ENUM для ролі водія
CREATE TYPE driver_role_enum AS ENUM ('PRIMARY_DRIVER', 'CO_DRIVER');

-- Таблиця учасників бронювання
CREATE TABLE booking_drivers (
                               id BIGSERIAL PRIMARY KEY,
                               booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL,
                               role driver_role_enum NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT unique_booking_driver UNIQUE (booking_id, user_id)
);

-- Обмеження: максимум 3 водія на бронювання
CREATE OR REPLACE FUNCTION check_max_drivers()
RETURNS TRIGGER AS $$
DECLARE
driver_count INTEGER;
BEGIN
SELECT COUNT(*) INTO driver_count FROM booking_drivers WHERE booking_id = NEW.booking_id;
IF driver_count >= 3 THEN
        RAISE EXCEPTION 'Maximum 3 drivers allowed per booking';
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
