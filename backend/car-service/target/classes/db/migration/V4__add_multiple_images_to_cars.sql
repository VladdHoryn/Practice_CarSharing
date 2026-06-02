-- =====================================================
-- Car Service - Add multiple images support (keep image_url)
-- Version: V4
-- =====================================================

-- Додати колонку для масиву фото (якщо не існує)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='cars' AND column_name='images') THEN
        ALTER TABLE cars ADD COLUMN images TEXT[];
    END IF;
    
    -- Додати колонку для головного фото, якщо її немає
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='cars' AND column_name='primary_image') THEN
        ALTER TABLE cars ADD COLUMN primary_image VARCHAR(500);
    END IF;
END $$;

-- Ініціалізувати primary_image з image_url, якщо primary_image порожній
UPDATE cars SET primary_image = image_url 
WHERE image_url IS NOT NULL AND primary_image IS NULL;

-- Ініціалізувати images масив з image_url, якщо images порожній
UPDATE cars SET images = ARRAY[image_url] 
WHERE image_url IS NOT NULL AND (images IS NULL OR array_length(images, 1) IS NULL);