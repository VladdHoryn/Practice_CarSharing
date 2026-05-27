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
    ('Toyota', 'Yaris', 2022, 'ECONOMY', 25.00, 2, 'AVAILABLE', 'https://images.carsguide.com.au/image/upload/e_trim:10,f_auto,c_scale,t_cg_base,w_678/v1/editorial/vhs/2021-toyota-yaris-index.png'),
    ('Honda', 'Fit', 2023, 'ECONOMY', 28.00, 2, 'AVAILABLE', 'https://cdn.motor1.com/images/mgl/vxxVW6/s1/2023-honda-fit-rs.webp'),
    
    -- COMFORT class
    ('Toyota', 'Camry', 2023, 'COMFORT', 50.00, 2, 'AVAILABLE', 'https://di-enrollment-api.s3.amazonaws.com/toyota/models/2023/camry/Trims/LE+Hybrid.png'),
    ('Honda', 'Accord', 2023, 'COMFORT', 55.00, 2, 'RENTED', 'https://di-uploads-pod23.dealerinspire.com/rairdonshondaofmarysville/uploads/2021/10/2022-honda-accord-leader-1-e1635722025683.png'),
    ('Nissan', 'Altima', 2022, 'COMFORT', 48.00, 2, 'AVAILABLE', 'https://di-uploads-pod36.dealerinspire.com/nissanofrochesterinc/uploads/2022/03/22Nissan-Altima-SR-DeepBluePearl-Jellybean.png'),
    
    -- =====================================================
    -- OWNER 2 (Maria Garcia, user_id=3) - 6 cars
    -- =====================================================
    -- BUSINESS class
    ('BMW', '3 Series', 2024, 'BUSINESS', 90.00, 3, 'AVAILABLE', 'https://imageutils.oragdigital.com:5001/image/resize/920x?path=https://orag-vehicle-images.s3.us-west-2.amazonaws.com/2024/BMW/3_Series/cc_2024BMC220019_01_1280_668.png'),
    ('Mercedes-Benz', 'C-Class', 2024, 'BUSINESS', 100.00, 3, 'AVAILABLE', 'https://s7d9.scene7.com/is/image/streamcompanies/OUTROJELLY-24-MB-C-Class?$Original-Dimensions-RGB-PNG$'),
    ('Audi', 'A4', 2023, 'BUSINESS', 95.00, 3, 'MAINTENANCE', 'https://platform.cstatic-images.com/in/v2/stock_photos/c3724b3c-daf7-4d35-a6fa-ff69891b5214/3d3dbda1-7176-4a30-9475-dc94c0fa34a0.png'),
    
    -- LUXURY class
    ('BMW', 'X5', 2024, 'LUXURY', 150.00, 3, 'AVAILABLE', 'https://example.com/bmw-x5.jpg'),
    ('Mercedes-Benz', 'E-Class', 2024, 'LUXURY', 160.00, 3, 'AVAILABLE', 'https://example.com/mercedes-eclass.jpg'),
    ('Audi', 'Q7', 2024, 'LUXURY', 155.00, 3, 'AVAILABLE', 'https://example.com/audi-q7.jpg'),
    
    -- EXTRA cars
    ('Tesla', 'Model 3', 2024, 'BUSINESS', 140.00, 2, 'AVAILABLE', 'https://example.com/tesla-model3.jpg'),
    ('Tesla', 'Model Y', 2024, 'LUXURY', 180.00, 3, 'AVAILABLE', 'https://example.com/tesla-modely.jpg'),
    
    -- Additional cars with different statuses
    ('Ford', 'Focus', 2021, 'ECONOMY', 30.00, 2, 'AVAILABLE', 'https://example.com/ford-focus.jpg'),
    ('Hyundai', 'Elantra', 2022, 'COMFORT', 45.00, 2, 'AVAILABLE', 'https://example.com/hyundai-elantra.jpg'),
    ('Kia', 'Sportage', 2023, 'COMFORT', 60.00, 3, 'RENTED', 'https://example.com/kia-sportage.jpg'),
    ('Lexus', 'RX 350', 2024, 'LUXURY', 170.00, 3, 'AVAILABLE', 'https://example.com/lexus-rx350.jpg'),
    ('Volkswagen', 'Passat', 2022, 'BUSINESS', 85.00, 2, 'AVAILABLE', 'https://example.com/vw-passat.jpg');
