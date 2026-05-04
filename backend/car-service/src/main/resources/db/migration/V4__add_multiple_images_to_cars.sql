-- =====================================================
-- Car Service - Add multiple images support
-- Version: V4
-- =====================================================

-- Add image_urls array column
ALTER TABLE cars 
ADD COLUMN image_urls TEXT[];

-- Migrate existing image_url data to array
UPDATE cars 
SET image_urls = ARRAY[image_url] 
WHERE image_url IS NOT NULL AND image_urls IS NULL;

-- Optional: drop old single image_url column (after migration verified)
-- ALTER TABLE cars DROP COLUMN image_url;

-- Add comment for new column
COMMENT ON COLUMN cars.image_urls IS 'Array of image URLs for the car (multiple photos supported)';
