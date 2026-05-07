-- =====================================================
-- Car Service - Add soft delete support
-- Version: V6
-- =====================================================

-- Add deleted_at for soft delete
ALTER TABLE cars 
ADD COLUMN deleted_at TIMESTAMP;

-- Add index for soft delete queries
CREATE INDEX idx_cars_deleted_at ON cars(deleted_at);

-- Create view for active cars only (excluding soft-deleted)
CREATE VIEW active_cars AS
SELECT * FROM cars WHERE deleted_at IS NULL;

-- User table already has is_active (soft delete)
-- Add index for email (already unique, just ensure index exists)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Add composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_cars_status_deleted ON cars(status, deleted_at);
CREATE INDEX IF NOT EXISTS idx_cars_approval_status ON car_approval_status(approval_status);

COMMENT ON COLUMN cars.deleted_at IS 'Soft delete timestamp - non-null means car is deleted';
