DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM cars WHERE id = 1) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (1, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'toyota_yaris.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 2) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (2, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'honda_fit.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 3) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (3, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'toyota_camry.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 4) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (4, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'honda_accord.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 5) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (5, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'nissan_altima.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 6) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (6, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'bmw_3series.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 7) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (7, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'mercedes_cclass.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 8) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (8, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'audi_a4.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 9) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (9, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'bmw_x5.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 10) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (10, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'mercedes_eclass.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 11) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (11, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'audi_q7.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 12) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (12, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'tesla_model3.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 13) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (13, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'tesla_modely.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 14) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (14, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'ford_focus.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 15) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (15, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'hyundai_elantra.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 16) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (16, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'kia_sportage.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 17) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (17, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'lexus_rx350.png', 70, true, NOW());
END IF;

    IF EXISTS (SELECT 1 FROM cars WHERE id = 18) THEN
        INSERT INTO car_images (car_id, image_data, content_type, file_name, file_size, is_main, created_at)
        VALUES (18, decode('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D49444154789C6360606000000004000127345B040000000049454E44AE426082', 'hex'), 'image/png', 'vw_passat.png', 70, true, NOW());
END IF;
END $$;
