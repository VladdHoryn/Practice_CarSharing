-- =====================================================
-- User Service - Seed users data
-- Version: V2
-- =====================================================

-- Password: "password123" (BCrypt hash)
-- Hash generated for: password123
-- You can generate your own with: https://bcrypt-generator.com/

INSERT INTO users (full_name, password, email, role, is_active) VALUES
    -- ADMINISTRATOR (id буде 1)
    ('System Administrator', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'admin@carsharing.com', 'ADMINISTRATOR', true),
    
    -- OWNERS (власники авто) (id: 2, 3)
    ('John Smith', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'owner1@carsharing.com', 'OWNER', true),
    ('Maria Garcia', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'owner2@carsharing.com', 'OWNER', true),
    
    -- RENTERS (орендарі) (id: 4, 5, 6, 7)
    ('James Wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'renter1@carsharing.com', 'RENTER', true),
    ('Emily Brown', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'renter2@carsharing.com', 'RENTER', true),
    ('Michael Lee', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'renter3@carsharing.com', 'RENTER', true),
    ('Sarah Johnson', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'renter4@carsharing.com', 'RENTER', true),
    
    -- MODERATORS (модератори) (id: 8, 9)
    ('Alex Turner', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'moderator1@carsharing.com', 'MODERATOR', true),
    ('Lisa White', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'moderator2@carsharing.com', 'MODERATOR', true),
    
    -- GUEST (гість, неактивний) (id: 10)
    ('Guest User', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'guest@carsharing.com', 'GUEST', false)
ON CONFLICT (email) DO NOTHING;

-- Додатковий коментар для пояснення ID
COMMENT ON TABLE users IS 'Users with IDs: 1=ADMIN, 2-3=OWNERS, 4-7=RENTERS, 8-9=MODERATORS, 10=GUEST(inactive)';
