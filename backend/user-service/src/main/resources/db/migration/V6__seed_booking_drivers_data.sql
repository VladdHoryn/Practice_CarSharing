-- =====================================================
-- Booking Service - Seed booking_drivers data (safe version with casting)
-- Version: V6
-- =====================================================

-- Додаємо водіїв ТІЛЬКИ ДЛЯ ІСНУЮЧИХ бронювань
INSERT INTO booking_drivers (booking_id, user_id, role)
SELECT b.id, driver.user_id, driver.role::driver_role_enum
FROM (VALUES
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
        (11, 6, 'PRIMARY_DRIVER'), (11, 4, 'CO_DRIVER'), (11, 5, 'CO_DRIVER')
     ) AS driver(booking_num, user_id, role)
       INNER JOIN bookings b ON b.id = driver.booking_num
WHERE NOT EXISTS (
  SELECT 1 FROM booking_drivers bd
  WHERE bd.booking_id = driver.booking_num AND bd.user_id = driver.user_id
);
