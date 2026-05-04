-- =====================================================
-- Car Service - Create car_approval_status table
-- Version: V3
-- =====================================================

-- Create approval status enum
CREATE TYPE car_approval_status_enum AS ENUM ('CREATED', 'APPROVED', 'REJECTED');

-- Create car_approval_status table (1:1 with cars)
CREATE TABLE car_approval_status (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL UNIQUE REFERENCES cars(id) ON DELETE CASCADE,
    approval_status car_approval_status_enum NOT NULL DEFAULT 'CREATED',
    document_urls TEXT[],
    document_names TEXT[],
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_car_approval_status_car_id ON car_approval_status(car_id);
CREATE INDEX idx_car_approval_status_approval_status ON car_approval_status(approval_status);

-- Add comments
COMMENT ON TABLE car_approval_status IS 'Car approval status (1:1 with cars). Controls visibility to regular users.';
COMMENT ON COLUMN car_approval_status.approval_status IS 'CREATED (pending), APPROVED (visible), REJECTED (not visible)';
COMMENT ON COLUMN car_approval_status.document_urls IS 'Array of document URLs (license, registration, insurance)';
COMMENT ON COLUMN car_approval_status.reviewed_by IS 'Reference to user-service (MODERATOR or ADMIN)';

-- Initialize for existing cars (set all as APPROVED)
INSERT INTO car_approval_status (car_id, approval_status, submitted_at, reviewed_at)
SELECT id, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM cars
ON CONFLICT (car_id) DO NOTHING;
