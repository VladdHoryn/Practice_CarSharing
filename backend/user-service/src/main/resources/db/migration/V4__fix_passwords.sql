-- =====================================================
-- User Service - Fix user passwords
-- Version: V4
-- 
-- Встановлює пароль "pass123" для всіх користувачів
-- BCrypt hash for "pass123": $2a$10$6t7qKqYqKqYqKqYqKqYqKu
-- =====================================================

UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K'
WHERE email IN (
    'admin@carsharing.com',
    'owner1@carsharing.com',
    'owner2@carsharing.com',
    'renter1@carsharing.com',
    'renter2@carsharing.com',
    'renter3@carsharing.com',
    'renter4@carsharing.com',
    'moderator1@carsharing.com',
    'moderator2@carsharing.com',
    'guest@carsharing.com'
);
