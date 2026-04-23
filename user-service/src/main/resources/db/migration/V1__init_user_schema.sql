-- =====================================================
-- USER SERVICE - Initial Schema Migration
-- =====================================================

-- =====================================================
-- ENUMS
-- =====================================================

CREATE TYPE user_role AS ENUM ('RENTER', 'OWNER', 'MODERATOR', 'ADMINISTRATOR');

-- =====================================================
-- TABLE
-- =====================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
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
-- INDEXES
-- =====================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- =====================================================
-- SEED DATA
-- =====================================================

-- Password: "password123" (BCrypt hash)
INSERT INTO users (email, password_hash, full_name, phone, role)
VALUES
  ('admin@carsharing.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'System Administrator', '+380501234567', 'ADMINISTRATOR'),
  ('renter1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'John Doe', '+380671234567', 'RENTER'),
  ('owner1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'Jane Smith', '+380931234567', 'OWNER'),
  ('moderator@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'Bob Johnson', '+380501234568', 'MODERATOR');
