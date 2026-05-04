-- =====================================================
-- Booking Service - Seed booking_drivers data
-- Version: V6
-- =====================================================

-- Booking 1 (CREATED) - 2 drivers
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (1, 4, 'PRIMARY_DRIVER'),
    (1, 5, 'CO_DRIVER');

-- Booking 2 (CREATED) - 1 driver only
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (2, 5, 'PRIMARY_DRIVER');

-- Booking 3 (PENDING) - 3 drivers (max allowed)
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (3, 6, 'PRIMARY_DRIVER'),
    (3, 4, 'CO_DRIVER'),
    (3, 7, 'CO_DRIVER');

-- Booking 4 (PENDING) - 2 drivers
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (4, 7, 'PRIMARY_DRIVER'),
    (4, 5, 'CO_DRIVER');

-- Booking 5 (CONFIRMED) - 2 drivers
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (5, 4, 'PRIMARY_DRIVER'),
    (5, 6, 'CO_DRIVER');

-- Booking 6 (CONFIRMED) - 1 driver
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (6, 5, 'PRIMARY_DRIVER');

-- Booking 7 (CONFIRMED) - 2 drivers
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (7, 6, 'PRIMARY_DRIVER'),
    (7, 7, 'CO_DRIVER');

-- Booking 8 (COMPLETED) - 2 drivers
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (8, 4, 'PRIMARY_DRIVER'),
    (8, 5, 'CO_DRIVER');

-- Booking 9 (COMPLETED) - 1 driver
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (9, 5, 'PRIMARY_DRIVER');

-- Booking 10 (COMPLETED) - 2 drivers
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (10, 7, 'PRIMARY_DRIVER'),
    (10, 4, 'CO_DRIVER');

-- Booking 11 (CANCELLED) - 3 drivers (max allowed)
INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (11, 6, 'PRIMARY_DRIVER'),
    (11, 4, 'CO_DRIVER'),
    (11, 5, 'CO_DRIVER');

-- Verify no booking exceeds 3 drivers
DO $$
DECLARE
    booking_id_var RECORD;
    driver_count INTEGER;
BEGIN
    FOR booking_id_var IN SELECT DISTINCT booking_id FROM booking_drivers LOOP
        SELECT COUNT(*) INTO driver_count 
        FROM booking_drivers 
        WHERE booking_id = booking_id_var.booking_id;
        
        IF driver_count > 3 THEN
            RAISE EXCEPTION 'Booking % has % drivers (exceeds limit of 3)', 
                booking_id_var.booking_id, driver_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'All bookings have valid number of drivers (max 3)';
END $$;
