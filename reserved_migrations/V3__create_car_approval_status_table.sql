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
