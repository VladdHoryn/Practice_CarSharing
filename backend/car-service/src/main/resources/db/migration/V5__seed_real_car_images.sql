-- =====================================================
-- Car Service - Seed real car images
-- Version: V5
-- =====================================================

-- Використовуємо Unsplash для реальних фото автомобілів
-- Оновлюємо існуючі авто з реальними зображеннями

-- Toyota Yaris (id=1)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800',
        'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800'
WHERE id = 1;

-- Honda Fit (id=2)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800',
        'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800'
WHERE id = 2;

-- Toyota Camry (id=3)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800',
        'https://images.unsplash.com/photo-1550355291-bbee04a92027?w=800',
        'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800'
WHERE id = 3;

-- Honda Accord (id=4)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800',
        'https://images.unsplash.com/photo-1607860108855-64acf2078ed9?w=800'
    ],
    primary_image = 'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800'
WHERE id = 4;

-- Nissan Altima (id=5)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1603584173870-7f23fdae1b7a?w=800'
    ],
    primary_image = 'https://images.unsplash.com/photo-1603584173870-7f23fdae1b7a?w=800'
WHERE id = 5;

-- BMW 3 Series (id=6)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800',
        'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800'
WHERE id = 6;

-- Mercedes C-Class (id=7)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800',
        'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800'
WHERE id = 7;

-- Audi A4 (id=8)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800',
        'https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800'
    ],
    primary_image = 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800'
WHERE id = 8;

-- BMW X5 (id=9)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1523961131990-5ea7c61b2107?w=800',
        'https://images.unsplash.com/photo-1523961131990-5ea7c61b2107?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1523961131990-5ea7c61b2107?w=800'
WHERE id = 9;

-- Mercedes E-Class (id=10)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800',
        'https://images.unsplash.com/photo-1607860108855-64acf2078ed9?w=800'
    ],
    primary_image = 'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800'
WHERE id = 10;

-- Audi Q7 (id=11)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800',
        'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800'
WHERE id = 11;

-- Tesla Model 3 (id=12)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800',
        'https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800'
WHERE id = 12;

-- Tesla Model Y (id=13)
UPDATE cars SET 
    images = ARRAY[
        'https://images.unsplash.com/photo-1617704548623-340376564e68?w=800',
        'https://images.unsplash.com/photo-1617704548623-340376564e68?w=400'
    ],
    primary_image = 'https://images.unsplash.com/photo-1617704548623-340376564e68?w=800'
WHERE id = 13;

-- =====================================================
-- Перевірка
-- =====================================================
DO $$
DECLARE
    car_record RECORD;
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Car images updated successfully!';
    
    FOR car_record IN 
        SELECT id, brand, model, primary_image, array_length(images, 1) as image_count 
        FROM cars 
        ORDER BY id
    LOOP
        RAISE NOTICE 'Car #%: % % - % images, primary: %', 
            car_record.id, 
            car_record.brand, 
            car_record.model, 
            COALESCE(car_record.image_count, 0),
            SUBSTRING(car_record.primary_image, 1, 50);
    END LOOP;
END $$;
