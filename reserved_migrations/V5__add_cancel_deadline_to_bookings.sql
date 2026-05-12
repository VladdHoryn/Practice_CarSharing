-- =====================================================
-- Booking Service - Add cancel_deadline column
-- Version: V5
-- =====================================================

-- Add cancel_deadline column
ALTER TABLE bookings 
ADD COLUMN cancel_deadline TIMESTAMP;

-- Set cancel_deadline based on start_date (can't cancel within 24 hours of start)
UPDATE bookings 
SET cancel_deadline = start_date - INTERVAL '1 day'
WHERE cancel_deadline IS NULL;

-- Make it NOT NULL after setting values
ALTER TABLE bookings 
ALTER COLUMN cancel_deadline SET NOT NULL;

-- Add check constraint: cancel_deadline must be before start_date
ALTER TABLE bookings 
ADD CONSTRAINT check_cancel_deadline_before_start 
CHECK (cancel_deadline < start_date);
