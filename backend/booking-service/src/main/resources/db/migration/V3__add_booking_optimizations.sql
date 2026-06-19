CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);

CREATE INDEX idx_bookings_car_dates ON bookings(car_id, start_date, end_date);

CREATE INDEX idx_bookings_status_dates ON bookings(status, start_date, end_date);

CREATE INDEX idx_bookings_cancel_deadline ON bookings(cancel_deadline) WHERE status IN ('CREATED', 'PENDING', 'CONFIRMED');

CREATE INDEX idx_bookings_created_at ON bookings(created_at);

CREATE OR REPLACE FUNCTION check_booking_overlap()
RETURNS TRIGGER AS $$
BEGIN

    IF EXISTS (
        SELECT 1 FROM bookings
        WHERE car_id = NEW.car_id
          AND id != NEW.id
          AND status IN ('CONFIRMED', 'PENDING')
    ) THEN
        RAISE EXCEPTION 'Car % is already booked for the requested period', NEW.car_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_prevent_overlap ON bookings;
CREATE TRIGGER trigger_prevent_overlap
    BEFORE INSERT OR UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION check_booking_overlap();

CREATE OR REPLACE FUNCTION update_booking_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_booking_updated_at ON bookings;
CREATE TRIGGER trigger_booking_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_booking_updated_at();

CREATE OR REPLACE FUNCTION validate_booking_dates()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status IN ('CREATED', 'PENDING', 'CONFIRMED')
       AND NEW.start_date < CURRENT_TIMESTAMP - INTERVAL '1 day'
    THEN
        RAISE EXCEPTION 'Start date cannot be in the past';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_validate_booking_dates
  BEFORE INSERT OR UPDATE ON bookings
                     FOR EACH ROW
                     EXECUTE FUNCTION validate_booking_dates();
