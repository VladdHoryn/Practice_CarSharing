-- =====================================================
-- User Service Database Initialization
-- PostgreSQL
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- ENUM type for user roles
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('RENTER', 'OWNER', 'MODERATOR', 'ADMINISTRATOR');
    END IF;
END$$;

-- =====================================================
-- Users table (only what's needed)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role user_role NOT NULL DEFAULT 'RENTER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================
-- Indexes
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- =====================================================
-- Seed data (for testing)
-- =====================================================
-- Password: "password123" (BCrypt hash)
INSERT INTO users (id, email, password_hash, full_name, phone, role)
VALUES 
    (gen_random_uuid(), 'admin@carsharing.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'System Administrator', '+380501234567', 'ADMINISTRATOR'),
    (gen_random_uuid(), 'renter1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'John Doe', '+380671234567', 'RENTER'),
    (gen_random_uuid(), 'owner1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'Jane Smith', '+380931234567', 'OWNER'),
    (gen_random_uuid(), 'moderator@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'Bob Johnson', '+380501234568', 'MODERATOR')
ON CONFLICT (email) DO NOTHING;

COMMENT ON TABLE users IS 'System users with roles: RENTER, OWNER, MODERATOR, ADMINISTRATOR';
