-- =====================================================
-- Car Service - Add multiple images support
-- Version: V4
-- =====================================================

-- Додати колонку для масиву фото
ALTER TABLE cars ADD COLUMN images TEXT[];

-- Додати колонку для головного фото
ALTER TABLE cars ADD COLUMN primary_image VARCHAR(500);

-- Перенести існуючі фото (якщо є колонка image_url)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='cars' AND column_name='image_url') THEN
        EXECUTE 'UPDATE cars SET images = ARRAY[image_url], primary_image = image_url WHERE image_url IS NOT NULL';
        EXECUTE 'ALTER TABLE cars DROP COLUMN image_url';
    END IF;
END $$;
