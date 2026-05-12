-- =====================================================
-- Payment Service - Seed payments data
-- Version: V2
-- =====================================================

INSERT INTO payments (booking_id, amount, method, status, payment_date) VALUES
    (1, 75.00, 'GOOGLE_PAY', 'PENDING', NULL),
    (2, 100.00, 'CARD', 'FAILED', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (3, 450.00, 'APPLE_PAY', 'SUCCESS', CURRENT_TIMESTAMP),
    (4, 300.00, 'CARD', 'PENDING', NULL),
    (5, 196.00, 'GOOGLE_PAY', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (6, 220.00, 'CARD', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (7, 420.00, 'APPLE_PAY', 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (8, 240.00, 'CARD', 'REFUNDED', CURRENT_TIMESTAMP - INTERVAL '25 days'),
    (9, 300.00, 'GOOGLE_PAY', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '17 days'),
    (10, 360.00, 'CARD', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '13 days'),
    (11, 380.00, 'GOOGLE_PAY', 'REFUNDED', CURRENT_TIMESTAMP - INTERVAL '5 days');
