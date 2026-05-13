-- =====================================================
-- Booking Service - Add indexes, constraints, triggers
-- Version: V3
-- =====================================================

-- =====================================================
-- 1. Foreign Key Constraints
-- =====================================================

-- Foreign key for user_id (logical reference to user-service)
-- Physical FK not possible across services, handled at app level

-- =====================================================
-- 2. Basic Indexes
-- =====================================================

-- Index for user_id (бронювання користувача)
CREATE INDEX idx_bookings_user_id ON bookings(user_id);

-- Index for car_id (бронювання авто)
CREATE INDEX idx_bookings_car_id ON bookings(car_id);

-- Index for status (фільтрація за статусом)
CREATE INDEX idx_bookings_status ON bookings(status);

-- =====================================================
-- 3. Composite Indexes for common query patterns
-- =====================================================

-- Composite index for user + status (мої бронювання за статусом)
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);

-- Composite index for car + status (бронювання конкретного авто за статусом)
CREATE INDEX idx_bookings_car_status ON bookings(car_id, status);

-- Composite index for date range queries (пошук по датах)
CREATE INDEX idx_bookings_dates ON bookings(start_date, end_date);

-- Composite index for active bookings (підтверджені, не завершені)
CREATE INDEX idx_bookings_active ON bookings(car_id, status, start_date) 
    WHERE status IN ('CONFIRMED', 'PENDING');

-- =====================================================
-- 4. Prevent overlapping bookings (EXCLUDE constraint)
-- =====================================================

-- Drop existing constraint if any
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS no_overlapping_bookings;

-- Add exclusion constraint to prevent overlapping bookings for same car
-- Requires btree_gist extension
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings ADD CONSTRAINT no_overlapping_bookings
    EXCLUDE USING gist (
        car_id WITH =,
        daterange(start_date, end_date, '[)') WITH &&
    ) WHERE (status IN ('CONFIRMED', 'PENDING'));

COMMENT ON CONSTRAINT no_overlapping_bookings ON bookings IS 
    'Prevents overlapping confirmed/pending bookings for the same car';

-- =====================================================
-- 5. Updated_at trigger
-- =====================================================

CREATE OR REPLACE FUNCTION update_bookings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_bookings_updated_at ON bookings;
CREATE TRIGGER trigger_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_bookings_updated_at();

-- =====================================================
-- 6. Additional check constraints
-- =====================================================

-- Ensure start_date is not in the past for new confirmed bookings
ALTER TABLE bookings ADD CONSTRAINT check_start_date_future 
    CHECK (status != 'CONFIRMED' OR start_date > CURRENT_TIMESTAMP);

-- Ensure total_price is positive (already exists, just for safety)
ALTER TABLE bookings ADD CONSTRAINT check_total_price_positive 
    CHECK (total_price > 0);

-- =====================================================
-- 7. Comments
-- =====================================================

COMMENT ON INDEX idx_bookings_user_status IS 'Optimizes queries for user bookings by status';
COMMENT ON INDEX idx_bookings_car_status IS 'Optimizes queries for car bookings by status';
COMMENT ON INDEX idx_bookings_dates IS 'Optimizes date range queries';
COMMENT ON INDEX idx_bookings_active IS 'Partial index for active (confirmed/pending) bookings';
