-- Додати колонку для головного фото (якщо її немає)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='cars' AND column_name='image_url') THEN
        ALTER TABLE cars ADD COLUMN image_url VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='cars' AND column_name='primary_image') THEN
        ALTER TABLE cars ADD COLUMN primary_image VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='cars' AND column_name='images') THEN
        ALTER TABLE cars ADD COLUMN images TEXT[];
    END IF;
END $$;

-- Перенести дані з image_url в primary_image (якщо primary_image порожній)
UPDATE cars SET primary_image = image_url 
WHERE image_url IS NOT NULL AND primary_image IS NULL;

-- Ініціалізувати images масив
UPDATE cars SET images = ARRAY[image_url] 
WHERE image_url IS NOT NULL AND (images IS NULL OR array_length(images, 1) IS NULL);