-- =====================================================
-- Car Service - Create car_images table
-- Version: V4
-- =====================================================

-- Створення таблиці для зберігання фотографій авто
CREATE TABLE IF NOT EXISTS car_images (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    image_data BYTEA NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Індекси для оптимізації
CREATE INDEX idx_car_images_car_id ON car_images(car_id);
CREATE INDEX idx_car_images_is_main ON car_images(is_main);

-- Видалення старої колонки image_url (якщо існує)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='cars' AND column_name='image_url') THEN
        ALTER TABLE cars DROP COLUMN image_url;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='cars' AND column_name='images') THEN
        ALTER TABLE cars DROP COLUMN images;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='cars' AND column_name='primary_image') THEN
        ALTER TABLE cars DROP COLUMN primary_image;
    END IF;
END $$;

-- Коментарі
COMMENT ON TABLE car_images IS 'Stores car photos (main image + gallery)';
COMMENT ON COLUMN car_images.image_data IS 'Binary image data (BLOB)';
COMMENT ON COLUMN car_images.is_main IS 'Flag indicating if this is the main image for the car';