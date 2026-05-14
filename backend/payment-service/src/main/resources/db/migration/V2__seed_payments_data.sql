-- =====================================================
-- Payment Service - Seed payments data
-- Version: V2 (updated for V3 constraints)
-- =====================================================

INSERT INTO payments (
  booking_id,
  amount,
  method,
  status,
  payment_date,
  provider_payment_id,
  currency,
  idempotency_key,
  client_secret
) VALUES

-- 1
(1, 75.00, 'GOOGLE_PAY', 'PENDING', CURRENT_TIMESTAMP, NULL, 'USD', 'idem_1', NULL),
-- 2
(2, 100.00, 'CARD', 'FAILED', CURRENT_TIMESTAMP - INTERVAL '1 day', 'prov_2', 'USD', 'idem_2', NULL),
-- 3
(3, 450.00, 'APPLE_PAY', 'SUCCESS', CURRENT_TIMESTAMP, 'prov_3', 'EUR', 'idem_3', NULL),
-- 4
(4, 300.00, 'CARD', 'PENDING', CURRENT_TIMESTAMP, NULL, 'USD', 'idem_4', NULL),
-- 5
(5, 196.00, 'GOOGLE_PAY', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '2 days', 'prov_5', 'EUR', 'idem_5', NULL),
-- 6
(6, 220.00, 'CARD', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '3 days', 'prov_6', 'USD', 'idem_6', NULL),
-- 7
(7, 420.00, 'APPLE_PAY', 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, 'USD', 'idem_7', NULL),
-- 8
(8, 240.00, 'CARD', 'REFUNDED', CURRENT_TIMESTAMP - INTERVAL '25 days', 'prov_8', 'EUR', 'idem_8', NULL),
-- 9
(9, 300.00, 'GOOGLE_PAY', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '17 days', 'prov_9', 'USD', 'idem_9', NULL),
-- 10
(10, 360.00, 'CARD', 'SUCCESS', CURRENT_TIMESTAMP - INTERVAL '13 days', 'prov_10', 'EUR', 'idem_10', NULL),
-- 11
(11, 380.00, 'GOOGLE_PAY', 'REFUNDED', CURRENT_TIMESTAMP - INTERVAL '5 days', 'prov_11', 'USD', 'idem_11', NULL);
