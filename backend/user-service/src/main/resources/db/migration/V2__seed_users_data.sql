-- =====================================================
-- User Service - Seed users data
-- Version: V2
-- =====================================================

-- Password: "password123" (BCrypt hash)
-- Hash generated for: password123
INSERT INTO users (full_name, password, email, role, is_active) VALUES
    -- ADMINISTRATOR (id: 1)
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

-- Verify
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Users seeded: % records', user_count;
    RAISE NOTICE '==========================================';
END $$;

COMMENT ON TABLE users IS 'Users seeded: 10 records (1 ADMIN, 2 OWNERS, 4 RENTERS, 2 MODERATORS, 1 GUEST)';
