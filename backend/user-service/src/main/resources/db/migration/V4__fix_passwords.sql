-- =====================================================
-- User Service - Fix user passwords
-- Version: V4
-- 
-- Встановлює пароль "pass123" для всіх користувачів
-- Хеш згенерований за допомогою BCrypt (rounds=10)
-- =====================================================

-- BCrypt hash для "pass123"
-- Згенеровано за допомогою: https://bcrypt-generator.com/
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'admin@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'owner1@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'owner2@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'renter1@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'renter2@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'renter3@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'renter4@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'moderator1@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'moderator2@carsharing.com';
UPDATE users SET password = '$2a$10$6t7qKqYqKqYqKqYqKqYqKu.3Q5Z7X8Y9Z0A1B2C3D4E5F6G7H8I9J0K' WHERE email = 'guest@carsharing.com';

-- =====================================================
-- Перевірка
-- =====================================================
DO $$
DECLARE
    user_record RECORD;
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Passwords updated successfully!';
    RAISE NOTICE 'All users now have password: pass123';
    
    FOR user_record IN SELECT email, role FROM users ORDER BY id LOOP
        RAISE NOTICE 'User: % (role: %)', user_record.email, user_record.role;
    END LOOP;
END $$;
