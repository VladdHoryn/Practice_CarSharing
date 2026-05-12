-- =====================================================
-- Car Service - Add updated_at to cars
-- Version: V5
-- =====================================================

ALTER TABLE cars ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
