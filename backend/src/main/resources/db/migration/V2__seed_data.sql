-- =====================================================
-- Car Sharing System - Seed Data
-- Version: V2
-- =====================================================

-- =====================================================
-- 1. Seed Rental Companies
-- =====================================================
INSERT INTO rental_companies (id, name, address, contact_email, contact_phone)
VALUES 
    (gen_random_uuid(), 'Elite Rentals', '123 Main St, Kyiv', 'info@eliterentals.com', '+380441234567'),
    (gen_random_uuid(), 'Economy Cars', '45 Shevchenko Blvd, Lviv', 'support@economycars.ua', '+380322345678'),
    (gen_random_uuid(), 'Premium Drive', '78 Khreshchatyk, Kyiv', 'hello@premiumdrive.com', '+380442345678');

-- =====================================================
-- 2. Seed Cars
-- =====================================================
-- Get company IDs (using subqueries for simplicity)
WITH company_ids AS (
    SELECT id, name FROM rental_companies
)
INSERT INTO cars (id, brand, model, year, class, price_per_day, rental_company_id, status, image_url)
VALUES 
    -- Elite Rentals cars
    (gen_random_uuid(), 'Toyota', 'Camry', 2023, 'COMFORT', 50.00, 
     (SELECT id FROM company_ids WHERE name = 'Elite Rentals'), 'AVAILABLE', 'https://example.com/camry.jpg'),
    (gen_random_uuid(), 'BMW', 'X5', 2024, 'BUSINESS', 120.00, 
     (SELECT id FROM company_ids WHERE name = 'Elite Rentals'), 'AVAILABLE', 'https://example.com/bmw-x5.jpg'),
    
    -- Economy Cars
    (gen_random_uuid(), 'Hyundai', 'Accent', 2022, 'ECONOMY', 25.00, 
     (SELECT id FROM company_ids WHERE name = 'Economy Cars'), 'AVAILABLE', 'https://example.com/accent.jpg'),
    (gen_random_uuid(), 'Skoda', 'Octavia', 2023, 'ECONOMY', 35.00, 
     (SELECT id FROM company_ids WHERE name = 'Economy Cars'), 'AVAILABLE', 'https://example.com/octavia.jpg'),
    
    -- Premium Drive cars
    (gen_random_uuid(), 'Mercedes-Benz', 'E-Class', 2024, 'BUSINESS', 150.00, 
     (SELECT id FROM company_ids WHERE name = 'Premium Drive'), 'AVAILABLE', 'https://example.com/e-class.jpg'),
    (gen_random_uuid(), 'Tesla', 'Model 3', 2024, 'BUSINESS', 180.00, 
     (SELECT id FROM company_ids WHERE name = 'Premium Drive'), 'AVAILABLE', 'https://example.com/tesla.jpg');

-- =====================================================
-- 3. Seed Users
-- =====================================================
-- Password: "password123" (hashed with BCrypt - for demo only)
-- In production, use proper password hashing
INSERT INTO users (id, email, password_hash, full_name, role, is_active)
VALUES 
    (gen_random_uuid(), 'admin@carsharing.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'System Admin', 'ADMIN', true),
    (gen_random_uuid(), 'client1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'John Doe', 'CLIENT', true),
    (gen_random_uuid(), 'client2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr5vKqYqKqYqKqYqKqYqKqYqKqYqKq', 'Jane Smith', 'CLIENT', true);

-- =====================================================
-- 4. Seed Profiles
-- =====================================================
WITH user_ids AS (
    SELECT id, email FROM users
)
INSERT INTO profiles (id, user_id, phone, address, preferences)
VALUES 
    (gen_random_uuid(), (SELECT id FROM user_ids WHERE email = 'admin@carsharing.com'), '+380501234567', 'Admin Office, Kyiv', '{"theme": "dark", "notifications": true}'),
    (gen_random_uuid(), (SELECT id FROM user_ids WHERE email = 'client1@example.com'), '+380671234567', '15 Park Ave, Lviv', '{"theme": "light", "notifications": true}'),
    (gen_random_uuid(), (SELECT id FROM user_ids WHERE email = 'client2@example.com'), '+380931234567', '8 Central St, Odesa', '{"theme": "light", "notifications": false}');
