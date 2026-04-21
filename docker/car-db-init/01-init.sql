-- =====================================================
-- Car Service Database Initialization
-- PostgreSQL
-- =====================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- ENUM types
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'car_status') THEN
        CREATE TYPE car_status AS ENUM ('AVAILABLE', 'RENTED', 'MAINTENANCE', 'UNAVAILABLE');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'car_class') THEN
        CREATE TYPE car_class AS ENUM ('ECONOMY', 'COMFORT', 'BUSINESS', 'LUXURY');
    END IF;
END$$;

-- =====================================================
-- Cars table
-- =====================================================
CREATE TABLE IF NOT EXISTS cars (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,                    -- посилання на user-service (USER з роллю OWNER)
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL CHECK (year >= 1990),
    class VARCHAR(30) NOT NULL CHECK (class IN ('ECONOMY', 'COMFORT', 'BUSINESS', 'LUXURY')),
    price_per_day DECIMAL(10,2) NOT NULL CHECK (price_per_day > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE', 'UNAVAILABLE')),
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    location_city VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================
-- Indexes
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_cars_owner_id ON cars(owner_id);
CREATE INDEX IF NOT EXISTS idx_cars_status ON cars(status);
CREATE INDEX IF NOT EXISTS idx_cars_location_city ON cars(location_city);

-- =====================================================
-- Seed data
-- =====================================================
INSERT INTO cars (id, owner_id, brand, model, year, class, price_per_day, status, license_plate, location_city, image_url)
VALUES 
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Toyota', 'Camry', 2023, 'COMFORT', 50.00, 'AVAILABLE', 'AA1234BB', 'Kyiv', 'https://example.com/camry.jpg'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'BMW', 'X5', 2024, 'LUXURY', 120.00, 'AVAILABLE', 'BB5678CC', 'Lviv', 'https://example.com/bmw.jpg'),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000002', 'Hyundai', 'Accent', 2022, 'ECONOMY', 25.00, 'AVAILABLE', 'CC9012DD', 'Odesa', NULL),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000002', 'Tesla', 'Model 3', 2024, 'BUSINESS', 150.00, 'AVAILABLE', 'DD3456EE', 'Kyiv', 'https://example.com/tesla.jpg')
ON CONFLICT (license_plate) DO NOTHING;
