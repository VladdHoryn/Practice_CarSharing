-- =====================================================
-- Booking Service - Seed booking_drivers data
-- Version: V6
-- =====================================================

INSERT INTO booking_drivers (
  booking_id,
  user_id,
  email,
  driver_code,
  status
)
SELECT
  data.booking_id,
  data.user_id,
  data.email,
  data.driver_code,
  data.status::booking_driver_status_enum
FROM (
       VALUES
         (1, 5, 'renter2@carsharing.com', 'RNT5PL91ZX', 'PENDING'),
         (1, 6, 'renter3@carsharing.com', 'RNT8CV24DF', 'ACCEPTED'),
         (2, 4, 'renter1@carsharing.com', 'RNT2GH68JK', 'PENDING'),
         (3, 7, 'renter4@carsharing.com', 'RNT1MN73QA', 'DECLINED'),
         (4, 5, 'renter2@carsharing.com', 'RNT5PL91ZX', 'ACCEPTED'),
         (5, 6, 'renter3@carsharing.com', 'RNT8CV24DF', 'PENDING'),
         (6, 7, 'renter4@carsharing.com', 'RNT1MN73QA', 'ACCEPTED'),
         (7, 4, 'renter1@carsharing.com', 'RNT2GH68JK', 'PENDING'),
         (8, 5, 'renter2@carsharing.com', 'RNT5PL91ZX', 'DECLINED'),
         (9, 6, 'renter3@carsharing.com', 'RNT8CV24DF', 'ACCEPTED'),
         (10, 4, 'renter1@carsharing.com', 'RNT2GH68JK', 'PENDING'),
         (11, 5, 'renter2@carsharing.com', 'RNT5PL91ZX', 'ACCEPTED')
     ) AS data(
               booking_id,
               user_id,
               email,
               driver_code,
               status
  )
WHERE NOT EXISTS (
  SELECT 1
  FROM booking_drivers bd
  WHERE bd.booking_id = data.booking_id
    AND bd.user_id = data.user_id
);
