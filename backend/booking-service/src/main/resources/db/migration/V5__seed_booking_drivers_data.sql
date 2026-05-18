-- =====================================================
-- Booking Service - Seed booking_drivers data
-- Version: V5
-- 
-- Важливо: user_id посилаються на user-service
--   RENTER IDs: 4, 5, 6, 7 (з таблиці users)
--   OWNER IDs: 2, 3 (також можуть бути водіями)
-- 
-- Обмеження: не більше 3 водіїв на одне бронювання
-- =====================================================

-- =====================================================
-- Додаємо водіїв для існуючих бронювань
-- =====================================================

-- Booking 1 (CREATED, user_id=4) - 2 водії
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (1, 4, 'PRIMARY_DRIVER'),
    (1, 5, 'CO_DRIVER');

-- Booking 2 (CREATED, user_id=5) - 1 водій
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (2, 5, 'PRIMARY_DRIVER');

-- Booking 3 (PENDING, user_id=6) - 3 водії (максимум)
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (3, 6, 'PRIMARY_DRIVER'),
    (3, 4, 'CO_DRIVER'),
    (3, 7, 'CO_DRIVER');

-- Booking 4 (PENDING, user_id=7) - 2 водії
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (4, 7, 'PRIMARY_DRIVER'),
    (4, 5, 'CO_DRIVER');

-- Booking 5 (CONFIRMED, user_id=4) - 2 водії
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (5, 4, 'PRIMARY_DRIVER'),
    (5, 6, 'CO_DRIVER');

-- Booking 6 (CONFIRMED, user_id=5) - 1 водій
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (6, 5, 'PRIMARY_DRIVER');

-- Booking 7 (CONFIRMED, user_id=6) - 2 водії
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (7, 6, 'PRIMARY_DRIVER'),
    (7, 7, 'CO_DRIVER');

-- Booking 8 (COMPLETED, user_id=4) - 2 водії
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (8, 4, 'PRIMARY_DRIVER'),
    (8, 5, 'CO_DRIVER');

-- Booking 9 (COMPLETED, user_id=5) - 1 водій
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (9, 5, 'PRIMARY_DRIVER');

-- Booking 10 (COMPLETED, user_id=7) - 2 водії
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (10, 7, 'PRIMARY_DRIVER'),
    (10, 4, 'CO_DRIVER');

-- Booking 11 (CANCELLED, user_id=6) - 3 водії (максимум)
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (11, 6, 'PRIMARY_DRIVER'),
    (11, 4, 'CO_DRIVER'),
    (11, 5, 'CO_DRIVER');

-- =====================================================
-- ДОДАТКОВІ ТЕСТОВІ БРОНЮВАННЯ З РІЗНОЮ КІЛЬКІСТЮ ВОДІЇВ
-- =====================================================

-- Додамо ще одне бронювання з 3 водіями для демонстрації
-- Booking 12 (нове, CONFIRMED)
INSERT INTO bookings (user_id, car_id, start_date, end_date, status, total_price, cancel_deadline)
SELECT 4, 10, 
       CURRENT_TIMESTAMP + INTERVAL '25 days', 
       CURRENT_TIMESTAMP + INTERVAL '28 days', 
       'CONFIRMED', 
       480.00,
       (CURRENT_TIMESTAMP + INTERVAL '25 days') - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE id = 12);

-- Додаємо водіїв для booking 12
INSERT INTO booking_drivers (booking_id, user_id, role)
SELECT 12, 4, 'PRIMARY_DRIVER'
WHERE NOT EXISTS (SELECT 1 FROM booking_drivers WHERE booking_id = 12 AND user_id = 4);

INSERT INTO booking_drivers (booking_id, user_id, role)
SELECT 12, 6, 'CO_DRIVER'
WHERE NOT EXISTS (SELECT 1 FROM booking_drivers WHERE booking_id = 12 AND user_id = 6);

INSERT INTO booking_drivers (booking_id, user_id, role)
SELECT 12, 7, 'CO_DRIVER'
WHERE NOT EXISTS (SELECT 1 FROM booking_drivers WHERE booking_id = 12 AND user_id = 7);

-- =====================================================
-- ПЕРЕВІРКА ОБМЕЖЕНЬ
-- =====================================================

-- Функція для перевірки, що жодне бронювання не перевищує 3 водіїв
DO $$
DECLARE
    booking_record RECORD;
    driver_count INTEGER;
    has_error BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Verifying booking_drivers constraints...';
    
    FOR booking_record IN SELECT DISTINCT booking_id FROM booking_drivers LOOP
        SELECT COUNT(*) INTO driver_count
        FROM booking_drivers
        WHERE booking_id = booking_record.booking_id;
        
        IF driver_count > 3 THEN
            RAISE WARNING '❌ Booking % has % drivers (exceeds limit of 3)', 
                booking_record.booking_id, driver_count;
            has_error := TRUE;
        ELSE
            RAISE NOTICE '✅ Booking % has % drivers', booking_record.booking_id, driver_count;
        END IF;
    END LOOP;
    
    IF NOT has_error THEN
        RAISE NOTICE '==========================================';
        RAISE NOTICE '✅ All bookings respect the max 3 drivers constraint!';
    END IF;
END $$;

-- =====================================================
-- СТАТИСТИКА
-- =====================================================
DO $$
DECLARE
    total_drivers INTEGER;
    total_bookings_with_drivers INTEGER;
    avg_drivers NUMERIC;
BEGIN
    SELECT COUNT(*) INTO total_drivers FROM booking_drivers;
    SELECT COUNT(DISTINCT booking_id) INTO total_bookings_with_drivers FROM booking_drivers;
    SELECT AVG(driver_count) INTO avg_drivers
    FROM (SELECT COUNT(*) as driver_count FROM booking_drivers GROUP BY booking_id) as counts;
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE '📊 Booking Drivers Statistics:';
    RAISE NOTICE '   Total driver records: %', total_drivers;
    RAISE NOTICE '   Bookings with drivers: %', total_bookings_with_drivers;
    RAISE NOTICE '   Average drivers per booking: %', ROUND(avg_drivers, 2);
END $$;

-- =====================================================
-- КОМЕНТАРІ
-- =====================================================
COMMENT ON TABLE booking_drivers IS 'Total records: 24 driver assignments across 12 bookings. Max 3 drivers per booking enforced.';
