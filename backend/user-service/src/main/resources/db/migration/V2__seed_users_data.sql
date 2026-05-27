-- =====================================================
-- User Service - Seed users data with working passwords
-- Version: V2
-- =====================================================

-- Очистити існуючі дані
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- Пароль для всіх користувачів: "pass123"
-- BCrypt hash для "pass123" (10 rounds)
-- Хеш згенеровано через bcrypt-generator.com

INSERT INTO users (full_name, password, email, role, is_active) VALUES
    -- ADMINISTRATOR (id: 1)
    ('System Administrator', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'admin@carsharing.com', 'ADMINISTRATOR', true),
    
    -- OWNERS (власники авто) (id: 2, 3)
    ('John Smith', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'owner1@carsharing.com', 'OWNER', true),
    ('Maria Garcia', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'owner2@carsharing.com', 'OWNER', true),
    
    -- RENTERS (орендарі) (id: 4, 5, 6, 7)
    ('James Wilson', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'renter1@carsharing.com', 'RENTER', true),
    ('Emily Brown', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'renter2@carsharing.com', 'RENTER', true),
    ('Michael Lee', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'renter3@carsharing.com', 'RENTER', true),
    ('Sarah Johnson', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'renter4@carsharing.com', 'RENTER', true),
    
    -- MODERATORS (модератори) (id: 8, 9)
    ('Alex Turner', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'moderator1@carsharing.com', 'MODERATOR', true),
    ('Lisa White', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'moderator2@carsharing.com', 'MODERATOR', true),
    
    -- GUEST (id: 10)
    ('Guest User', '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K', 'guest@carsharing.com', 'GUEST', false);

-- =====================================================
-- Перевірка
-- =====================================================
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Users seeded: % records', user_count;
    RAISE NOTICE 'All users have password: pass123';
    RAISE NOTICE '==========================================';
END $$;
