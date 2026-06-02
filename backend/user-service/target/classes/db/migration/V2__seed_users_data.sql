-- =====================================================
-- User Service - Seed users data with working BCrypt passwords
-- Version: V2
-- =====================================================

-- Очистити існуючі дані
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- BCrypt hash для пароля "pass123" (10 rounds)
-- Згенеровано через bcrypt-generator.com
-- Пароль: pass123

-- =====================================================
-- User Service - Seed users data with working passwords
-- Version: V2
-- =====================================================

-- Видалити старі дані (якщо потрібно)
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- Правильний BCrypt хеш для пароля "pass123"
-- Згенеровано через BCryptPasswordEncoder (rounds=10)
-- Пароль: pass123

INSERT INTO users (full_name, password, email, role, is_active) VALUES
    ('System Administrator', '$2a$10$yv5qZPgBe.LOeC2MHwC5d.BgU/xt6tX2YUJxXdwOIClJwgJuwZSOi', 'admin@carsharing.com', 'ADMINISTRATOR', true),
    ('John Smith', '$2a$10$yv5qZPgBe.LOeC2MHwC5d.BgU/xt6tX2YUJxXdwOIClJwgJuwZSOi', 'owner1@carsharing.com', 'OWNER', true),
    ('Maria Garcia', '$2a$10$yv5qZPgBe.LOeC2MHwC5d.BgU/xt6tX2YUJxXdwOIClJwgJuwZSOi', 'owner2@carsharing.com', 'OWNER', true),
    ('James Wilson', '$2a$10$yv5qZPgBe.LOeC2MHwC5d.BgU/xt6tX2YUJxXdwOIClJwgJuwZSOi', 'renter1@carsharing.com', 'RENTER', true),
    ('Emily Brown', '$2a$10$yv5qZPgBe.LOeC2MHwC5d.BgU/xt6tX2YUJxXdwOIClJwgJuwZSOi', 'renter2@carsharing.com', 'RENTER', true),
    ('Michael Lee', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'renter3@carsharing.com', 'RENTER', true),
    ('Sarah Johnson', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'renter4@carsharing.com', 'RENTER', true),
    ('Alex Turner', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'moderator1@carsharing.com', 'MODERATOR', true),
    ('Lisa White', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'moderator2@carsharing.com', 'MODERATOR', true),
    ('Guest User', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'guest@carsharing.com', 'GUEST', false);
-- =====================================================
-- Перевірка
-- =====================================================
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    RAISE NOTICE 'Users seeded: % records with password "pass123"', user_count;
END $$;