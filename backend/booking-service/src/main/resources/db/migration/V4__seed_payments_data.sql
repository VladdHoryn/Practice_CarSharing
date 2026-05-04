-- =====================================================
-- Booking Service - Seed payments data
-- Version: V4
-- =====================================================

INSERT INTO payments (booking_id, amount, method, status, transaction_id, payment_date) VALUES
    -- Booking 1 (CREATED) - payment not yet processed
    (1, 75.00, 'ONLINE', 'PENDING', NULL, NULL),
    
    -- Booking 2 (CREATED) - payment failed
    (2, 100.00, 'CARD', 'FAILED', 'txn_failed_001', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    
    -- Booking 3 (PENDING) - payment successful
    (3, 450.00, 'ONLINE', 'SUCCESS', 'txn_success_003', CURRENT_TIMESTAMP),
    
    -- Booking 4 (PENDING) - payment pending
    (4, 300.00, 'CARD', 'PENDING', NULL, NULL),
    
    -- Booking 5 (CONFIRMED) - payment successful
    (5, 196.00, 'ONLINE', 'SUCCESS', 'txn_success_005', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    
    -- Booking 6 (CONFIRMED) - payment successful
    (6, 220.00, 'CARD', 'SUCCESS', 'txn_success_006', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    
    -- Booking 7 (CONFIRMED) - payment successful
    (7, 420.00, 'ONLINE', 'SUCCESS', 'txn_success_007', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    
    -- Booking 8 (COMPLETED) - payment refunded
    (8, 240.00, 'CARD', 'REFUNDED', 'txn_refund_008', CURRENT_TIMESTAMP - INTERVAL '25 days'),
    
    -- Booking 9 (COMPLETED) - payment successful
    (9, 300.00, 'ONLINE', 'SUCCESS', 'txn_success_009', CURRENT_TIMESTAMP - INTERVAL '17 days'),
    
    -- Booking 10 (COMPLETED) - payment successful
    (10, 360.00, 'CASH', 'SUCCESS', NULL, CURRENT_TIMESTAMP - INTERVAL '13 days'),
    
    -- Booking 11 (CANCELLED) - payment refunded
    (11, 380.00, 'ONLINE', 'REFUNDED', 'txn_refund_011', CURRENT_TIMESTAMP - INTERVAL '5 days');

-- Verify
DO $$
DECLARE
    payment_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO payment_count FROM payments;
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Payments seeded: % records', payment_count;
    RAISE NOTICE '==========================================';
END $$;
