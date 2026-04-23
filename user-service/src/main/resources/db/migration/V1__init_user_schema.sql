-- =====================================================
-- User Service - Initial Schema Migration
-- Version: V1
-- Based on: User.java, UserRole.java
-- =====================================================

-- Create ENUM type for user roles (matches UserRole.java)
CREATE TYPE user_role_enum AS ENUM ('GUEST', 'RENTER', 'OWNER', 'MODERATOR', 'ADMINISTRATOR');

-- Create users table (matches User.java)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role user_role_enum NOT NULL,
    created_at DATE NOT NULL DEFAULT CURRENT_DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Add table comments
COMMENT ON TABLE users IS 'System users with authentication and role-based access';
COMMENT ON COLUMN users.id IS 'Auto-increment primary key (BIGSERIAL)';
COMMENT ON COLUMN users.full_name IS 'User full name (2-100 characters)';
COMMENT ON COLUMN users.password IS 'Hashed password (BCrypt recommended)';
COMMENT ON COLUMN users.email IS 'Unique email address for login';
COMMENT ON COLUMN users.role IS 'User role: GUEST, RENTER, OWNER, MODERATOR, ADMINISTRATOR';
COMMENT ON COLUMN users.created_at IS 'Account creation date';
COMMENT ON COLUMN users.is_active IS 'Soft delete flag - false means account is deactivated';

-- =====================================================
-- Seed data (for testing)
-- =====================================================
-- Password: "password123" (BCrypt hash)
INSERT INTO users (full_name, password, email, role, is_active)
VALUES 
    ('System Administrator', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'admin@carsharing.com', 'ADMINISTRATOR', true),
    ('John Renter', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'renter1@example.com', 'RENTER', true),
    ('Jane Owner', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'owner1@example.com', 'OWNER', true),
    ('Bob Moderator', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'moderator@example.com', 'MODERATOR', true),
    ('Guest User', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'guest@example.com', 'GUEST', true)
ON CONFLICT (email) DO NOTHING;
