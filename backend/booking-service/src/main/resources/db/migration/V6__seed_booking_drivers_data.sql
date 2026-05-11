-- =====================================================
-- Booking Service - Seed booking_drivers data
-- Version: V4
-- =====================================================

INSERT INTO booking_drivers (booking_id, user_id, role) VALUES
    (1, 4, 'PRIMARY_DRIVER'), (1, 5, 'CO_DRIVER'),
    (2, 5, 'PRIMARY_DRIVER'),
    (3, 6, 'PRIMARY_DRIVER'), (3, 4, 'CO_DRIVER'), (3, 7, 'CO_DRIVER'),
    (4, 7, 'PRIMARY_DRIVER'), (4, 5, 'CO_DRIVER'),
    (5, 4, 'PRIMARY_DRIVER'), (5, 6, 'CO_DRIVER'),
    (6, 5, 'PRIMARY_DRIVER'),
    (7, 6, 'PRIMARY_DRIVER'), (7, 7, 'CO_DRIVER'),
    (8, 4, 'PRIMARY_DRIVER'), (8, 5, 'CO_DRIVER'),
    (9, 5, 'PRIMARY_DRIVER'),
    (10, 7, 'PRIMARY_DRIVER'), (10, 4, 'CO_DRIVER'),
    (11, 6, 'PRIMARY_DRIVER'), (11, 4, 'CO_DRIVER'), (11, 5, 'CO_DRIVER');

DO $$
DECLARE
    driver_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO driver_count FROM booking_drivers;
    RAISE NOTICE 'Booking drivers seeded: %', driver_count;
END $$;
