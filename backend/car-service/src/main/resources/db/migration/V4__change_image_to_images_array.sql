-- =====================================================
-- Car Service - Change image_url to images array
-- Version: V4
-- =====================================================

-- Додати нову колонку для масиву фото
ALTER TABLE cars ADD COLUMN images TEXT[];

-- Перенести існуючі фото з image_url в масив
UPDATE cars SET images = ARRAY[image_url] WHERE image_url IS NOT NULL;

-- Додати колонку для головного фото (яке відображається в списку)
ALTER TABLE cars ADD COLUMN primary_image VARCHAR(500);

-- Встановити перше фото як primary_image, якщо воно є
UPDATE cars SET primary_image = image_url WHERE image_url IS NOT NULL;

-- Видалити стару колонку image_url
ALTER TABLE cars DROP COLUMN image_url;

-- Додати коментарі
COMMENT ON COLUMN cars.images IS 'Array of image URLs for the car gallery';
COMMENT ON COLUMN cars.primary_image IS 'Main image displayed in car listings';
