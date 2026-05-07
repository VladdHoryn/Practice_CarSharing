-- =====================================================
-- Car Service - Seed cars data (10+ items with valid user_id references)
-- Version: V2
-- 
-- Important: user_id references users from user-service:
--   OWNER 1: id=2 (John Smith)
--   OWNER 2: id=3 (Maria Garcia)
-- =====================================================

INSERT INTO cars (brand, model, year, car_class, price_per_day, user_id, status, image_url) VALUES
    -- =====================================================
    -- OWNER 1 (John Smith, user_id=2) - 5 cars
    -- =====================================================
    -- ECONOMY class
    ('Toyota', 'Yaris', 2022, 'ECONOMY', 25.00, 2, 'AVAILABLE', 'https://example.com/toyota-yaris.jpg'),
    ('Honda', 'Fit', 2023, 'ECONOMY', 28.00, 2, 'AVAILABLE', 'https://example.com/honda-fit.jpg'),
    
    -- COMFORT class
    ('Toyota', 'Camry', 2023, 'COMFORT', 50.00, 2, 'AVAILABLE', 'https://example.com/toyota-camry.jpg'),
    ('Honda', 'Accord', 2023, 'COMFORT', 55.00, 2, 'RENTED', 'https://example.com/honda-accord.jpg'),
    ('Nissan', 'Altima', 2022, 'COMFORT', 48.00, 2, 'AVAILABLE', 'https://example.com/nissan-altima.jpg'),
    
    -- =====================================================
    -- OWNER 2 (Maria Garcia, user_id=3) - 6 cars
    -- =====================================================
    -- BUSINESS class
    ('BMW', '3 Series', 2024, 'BUSINESS', 90.00, 3, 'AVAILABLE', 'https://example.com/bmw-3series.jpg'),
    ('Mercedes-Benz', 'C-Class', 2024, 'BUSINESS', 100.00, 3, 'AVAILABLE', 'https://example.com/mercedes-cclass.jpg'),
    ('Audi', 'A4', 2023, 'BUSINESS', 95.00, 3, 'MAINTENANCE', 'https://example.com/audi-a4.jpg'),
    
    -- LUXURY class
    ('BMW', 'X5', 2024, 'LUXURY', 150.00, 3, 'AVAILABLE', 'https://example.com/bmw-x5.jpg'),
    ('Mercedes-Benz', 'E-Class', 2024, 'LUXURY', 160.00, 3, 'AVAILABLE', 'https://example.com/mercedes-eclass.jpg'),
    ('Audi', 'Q7', 2024, 'LUXURY', 155.00, 3, 'AVAILABLE', 'https://example.com/audi-q7.jpg'),
    
    -- =====================================================
    -- EXTRA: Additional cars to reach 10+ (total: 11 cars)
    -- =====================================================
    ('Tesla', 'Model 3', 2024, 'BUSINESS', 140.00, 2, 'AVAILABLE', 'https://example.com/tesla-model3.jpg'),
    ('Tesla', 'Model Y', 2024, 'LUXURY', 180.00, 3, 'AVAILABLE', 'https://example.com/tesla-modely.jpg');

-- Verify the number of inserted cars
DO $$
DECLARE
    car_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO car_count FROM cars;
    IF car_count < 10 THEN
        RAISE NOTICE 'Warning: Only % cars were inserted, expected at least 10', car_count;
    ELSE
        RAISE NOTICE 'Success: % cars have been inserted', car_count;
    END IF;
END $$;

-- Add summary comment
COMMENT ON TABLE cars IS 'Total 12 cars inserted: OWNER1(id=2): 5 cars, OWNER2(id=3): 6 cars, EXTRA: 1 car';
