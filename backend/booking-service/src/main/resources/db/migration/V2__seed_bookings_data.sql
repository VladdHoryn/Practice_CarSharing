-- =====================================================
-- Booking Service - Seed bookings data (10+ items with valid references)
-- Version: V2
-- 
-- Important references:
--   user_id: references users from user-service
--     - RENTER IDs: 4, 5, 6, 7
--   car_id: references cars from car-service
--     - CAR IDs: 1-12 (from seed data)
-- =====================================================

-- =====================================================
-- BOOKINGS (11 bookings with different statuses and date ranges)
-- =====================================================

INSERT INTO bookings (user_id, car_id, start_date, end_date, status, total_price) VALUES

    -- =====================================================
    -- CREATED status (нові, ще не підтверджені)
    -- =====================================================
    -- RENTER 4 books CAR 1 (Toyota Yaris, ECONOMY, $25/day) for 3 days
    (4, 1, 
     CURRENT_TIMESTAMP + INTERVAL '10 days', 
     CURRENT_TIMESTAMP + INTERVAL '13 days', 
     'CREATED', 
     75.00),  -- 3 * 25 = 75
    
    -- RENTER 5 books CAR 3 (Toyota Camry, COMFORT, $50/day) for 2 days
    (5, 3, 
     CURRENT_TIMESTAMP + INTERVAL '7 days', 
     CURRENT_TIMESTAMP + INTERVAL '9 days', 
     'CREATED', 
     100.00),  -- 2 * 50 = 100

    -- =====================================================
    -- PENDING status (очікують підтвердження)
    -- =====================================================
    -- RENTER 6 books CAR 7 (BMW 3 Series, BUSINESS, $90/day) for 5 days
    (6, 7, 
     CURRENT_TIMESTAMP + INTERVAL '3 days', 
     CURRENT_TIMESTAMP + INTERVAL '8 days', 
     'PENDING', 
     450.00),  -- 5 * 90 = 450
    
    -- RENTER 7 books CAR 9 (BMW X5, LUXURY, $150/day) for 2 days
    (7, 9, 
     CURRENT_TIMESTAMP + INTERVAL '5 days', 
     CURRENT_TIMESTAMP + INTERVAL '7 days', 
     'PENDING', 
     300.00),  -- 2 * 150 = 300

    -- =====================================================
    -- CONFIRMED status (підтверджені, активні оренди)
    -- =====================================================
    -- RENTER 4 books CAR 2 (Honda Fit, ECONOMY, $28/day) for 7 days (starting tomorrow)
    (4, 2, 
     CURRENT_TIMESTAMP + INTERVAL '1 day', 
     CURRENT_TIMESTAMP + INTERVAL '8 days', 
     'CONFIRMED', 
     196.00),  -- 7 * 28 = 196
    
    -- RENTER 5 books CAR 4 (Honda Accord, COMFORT, $55/day) for 4 days
    (5, 4, 
     CURRENT_TIMESTAMP + INTERVAL '2 days', 
     CURRENT_TIMESTAMP + INTERVAL '6 days', 
     'CONFIRMED', 
     220.00),  -- 4 * 55 = 220
    
    -- RENTER 6 books CAR 11 (Tesla Model 3, BUSINESS, $140/day) for 3 days
    (6, 11, 
     CURRENT_TIMESTAMP + INTERVAL '15 days', 
     CURRENT_TIMESTAMP + INTERVAL '18 days', 
     'CONFIRMED', 
     420.00),  -- 3 * 140 = 420

    -- =====================================================
    -- COMPLETED status (завершені оренди - історичні дані)
    -- =====================================================
    -- RENTER 4 books CAR 5 (Nissan Altima, COMFORT, $48/day) for 5 days (past)
    (4, 5, 
     CURRENT_TIMESTAMP - INTERVAL '30 days', 
     CURRENT_TIMESTAMP - INTERVAL '25 days', 
     'COMPLETED', 
     240.00),  -- 5 * 48 = 240
    
    -- RENTER 5 books CAR 8 (Mercedes C-Class, BUSINESS, $100/day) for 3 days (past)
    (5, 8, 
     CURRENT_TIMESTAMP - INTERVAL '20 days', 
     CURRENT_TIMESTAMP - INTERVAL '17 days', 
     'COMPLETED', 
     300.00),  -- 3 * 100 = 300
    
    -- RENTER 7 books CAR 12 (Tesla Model Y, LUXURY, $180/day) for 2 days (past)
    (7, 12, 
     CURRENT_TIMESTAMP - INTERVAL '15 days', 
     CURRENT_TIMESTAMP - INTERVAL '13 days', 
     'COMPLETED', 
     360.00),  -- 2 * 180 = 360

    -- =====================================================
    -- CANCELLED status (скасовані оренди)
    -- =====================================================
    -- RENTER 6 books CAR 6 (Audi A4, BUSINESS, $95/day) for 4 days (cancelled)
    (6, 6, 
     CURRENT_TIMESTAMP + INTERVAL '20 days', 
     CURRENT_TIMESTAMP + INTERVAL '24 days', 
     'CANCELLED', 
     380.00);  -- 4 * 95 = 380

-- =====================================================
-- Verify the number of inserted bookings
-- =====================================================
DO $$
DECLARE
    booking_count INTEGER;
    status_report TEXT;
BEGIN
    SELECT COUNT(*) INTO booking_count FROM bookings;
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Bookings seeded successfully!';
    RAISE NOTICE 'Total bookings inserted: %', booking_count;
    RAISE NOTICE '==========================================';
    
    -- Show statistics by status
    FOR status_report IN 
        SELECT status::text, COUNT(*) as count 
        FROM bookings 
        GROUP BY status 
        ORDER BY status
    LOOP
        RAISE NOTICE '%', status_report;
    END LOOP;
    
    IF booking_count < 10 THEN
        RAISE WARNING 'Only % bookings were inserted, expected at least 10', booking_count;
    ELSE
        RAISE NOTICE 'SUCCESS: % bookings inserted (minimum requirement met)', booking_count;
    END IF;
END $$;

-- =====================================================
-- Summary comment
-- =====================================================
COMMENT ON TABLE bookings IS 'Total 11 bookings inserted across all statuses: 
- CREATED: 2 (future rentals waiting for submission)
- PENDING: 2 (submitted, waiting for confirmation)
- CONFIRMED: 3 (active confirmed rentals)
- COMPLETED: 3 (finished historical rentals)
- CANCELLED: 1 (cancelled rental)';

-- =====================================================
-- Additional useful queries for testing
-- =====================================================

-- View all bookings with human-readable information
COMMENT ON DATABASE booking_service_db IS '
Quick reference - Booking IDs by renter:
- RENTER 4 (id=4): bookings on cars 1,2,5 (statuses: CREATED, CONFIRMED, COMPLETED)
- RENTER 5 (id=5): bookings on cars 3,4,8 (statuses: CREATED, CONFIRMED, COMPLETED)
- RENTER 6 (id=6): bookings on cars 7,11,6 (statuses: PENDING, CONFIRMED, CANCELLED)
- RENTER 7 (id=7): bookings on cars 9,12 (statuses: PENDING, COMPLETED)
';
