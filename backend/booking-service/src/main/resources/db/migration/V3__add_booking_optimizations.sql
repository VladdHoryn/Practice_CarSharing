-- =====================================================
-- Booking Service - Foreign keys, indexes and overlapping check
-- Version: V3
-- =====================================================

-- Composite index for user + status (частий запит: бронювання конкретного користувача за статусом)
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);

-- Composite index for car + date range (оптимізація перевірки перетинів)
CREATE INDEX idx_bookings_car_dates ON bookings(car_id, start_date, end_date);

-- Composite index for status + date (для пошуку активних бронювань)
CREATE INDEX idx_bookings_status_dates ON bookings(status, start_date, end_date);

-- Index for cancel_deadline (для фонових задач скасування)
CREATE INDEX idx_bookings_cancel_deadline ON bookings(cancel_deadline) WHERE status IN ('CREATED', 'PENDING', 'CONFIRMED');

-- Index for created_at (для звітів та аналітики)
CREATE INDEX idx_bookings_created_at ON bookings(created_at);

-- =====================================================
-- 3. Prevent overlapping bookings (механізм на рівні БД)
-- =====================================================

-- Функція для перевірки перетину бронювань
CREATE OR REPLACE FUNCTION check_booking_overlap()
RETURNS TRIGGER AS $$
BEGIN
    -- Перевіряємо чи існує підтверджене бронювання на цей самий автомобіль
    -- з перетинаючимся діапазоном дат
    IF EXISTS (
        SELECT 1 FROM bookings
        WHERE car_id = NEW.car_id
          AND id != NEW.id
          AND status IN ('CONFIRMED', 'PENDING')
          AND daterange(start_date, end_date, '[)') && daterange(NEW.start_date, NEW.end_date, '[)')
    ) THEN
        RAISE EXCEPTION 'Car % is already booked for the requested period', NEW.car_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Додаємо тригер BEFORE INSERT OR UPDATE
DROP TRIGGER IF EXISTS trigger_prevent_overlap ON bookings;
CREATE TRIGGER trigger_prevent_overlap
    BEFORE INSERT OR UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION check_booking_overlap();

-- =====================================================
-- 4. Автоматичне оновлення updated_at
-- =====================================================

-- Функція вже існує в user-service, але створимо свою для booking-service
CREATE OR REPLACE FUNCTION update_booking_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Додаємо тригер
DROP TRIGGER IF EXISTS trigger_booking_updated_at ON bookings;
CREATE TRIGGER trigger_booking_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_booking_updated_at();
