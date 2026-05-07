-- =====================================================
-- Car Service - Add updated_at to cars
-- Version: V5
-- =====================================================

ALTER TABLE cars 
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Create function to auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for cars
CREATE TRIGGER update_cars_updated_at
    BEFORE UPDATE ON cars
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Also add updated_at to car_approval_status (already has it)
-- Add updated_at to booking_drivers (already has it)
-- Add updated_at to payments (already has it)
