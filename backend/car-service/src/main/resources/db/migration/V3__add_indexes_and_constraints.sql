-- =====================================================
-- Car Service - Add indexes, foreign keys, and triggers
-- Version: V3
-- =====================================================

-- =====================================================
-- 1. Foreign Key Constraints (reference to user-service)
-- =====================================================
-- NOTE: user_id references users(id) in user-service
-- This is a logical FK (can't create physical FK across services in microservices)
-- We'll rely on application-level validation

-- =====================================================
-- 2. Indexes for performance
-- =====================================================

-- Composite index for status + price (частий фільтр)
CREATE INDEX idx_cars_status_price ON cars(status, price_per_day);

-- Composite index for class + status (фільтрація за класом та статусом)
CREATE INDEX idx_cars_class_status ON cars(car_class, status);

-- Composite index for location + status (пошук авто в місті)
CREATE INDEX idx_cars_location_status ON cars(location_city, status);

-- Index for price range queries
CREATE INDEX idx_cars_price_range ON cars(price_per_day);

-- Index for year filtering
CREATE INDEX idx_cars_year ON cars(year);

-- Composite index for brand + model (пошук за маркою/моделлю)
CREATE INDEX idx_cars_brand_model ON cars(brand, model);

-- Index for user_id (власник авто)
CREATE INDEX idx_cars_user_id ON cars(user_id);

-- Partial index for available cars only
CREATE INDEX idx_cars_available_only ON cars(id, price_per_day) WHERE status = 'AVAILABLE';

-- =====================================================
-- 3. Updated_at trigger (спрощений)
-- =====================================================

-- Create function if not exists (check before creating)
CREATE OR REPLACE FUNCTION update_cars_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists, then create
DROP TRIGGER IF EXISTS trigger_cars_updated_at ON cars;
CREATE TRIGGER trigger_cars_updated_at
    BEFORE UPDATE ON cars
    FOR EACH ROW
    EXECUTE FUNCTION update_cars_updated_at();

-- =====================================================
-- 4. Comments for documentation
-- =====================================================

COMMENT ON INDEX idx_cars_status_price IS 'Optimizes filtering by status and price sorting';
COMMENT ON INDEX idx_cars_class_status IS 'Optimizes filtering by car class and status';
COMMENT ON INDEX idx_cars_location_status IS 'Optimizes search by city and availability';
COMMENT ON INDEX idx_cars_price_range IS 'Optimizes price range queries';
COMMENT ON INDEX idx_cars_year IS 'Optimizes year filtering';
COMMENT ON INDEX idx_cars_brand_model IS 'Optimizes brand/model search';
COMMENT ON INDEX idx_cars_user_id IS 'Optimizes queries for cars by owner';
COMMENT ON INDEX idx_cars_available_only IS 'Partial index for available cars queries';
