-- =====================================================
-- Car Service - Create cars table
-- Version: V1
-- Based on: Car.java, CarClass.java, CarStatus.java
-- =====================================================

-- Create ENUM types (matches Java enums)
CREATE TYPE car_class_enum AS ENUM ('ECONOMY', 'COMFORT', 'BUSINESS', 'LUXURY');
CREATE TYPE car_status_enum AS ENUM ('AVAILABLE', 'RENTED', 'MAINTENANCE');

-- Create cars table (matches Car.java)
CREATE TABLE cars (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL CHECK (year >= 1950),
    car_class car_class_enum NOT NULL,
    price_per_day DECIMAL(10,2) NOT NULL CHECK (price_per_day > 0),
    user_id BIGINT NOT NULL,
    status car_status_enum NOT NULL,
    image_url VARCHAR(500) CHECK (image_url IS NULL OR image_url ~ '^https?://.*')
);

-- Create indexes for performance
CREATE INDEX idx_cars_user_id ON cars(user_id);
CREATE INDEX idx_cars_status ON cars(status);
CREATE INDEX idx_cars_car_class ON cars(car_class);
CREATE INDEX idx_cars_price_per_day ON cars(price_per_day);

-- Add table comments
COMMENT ON TABLE cars IS 'Cars available for rent on the platform';
COMMENT ON COLUMN cars.id IS 'Auto-increment primary key (BIGSERIAL)';
COMMENT ON COLUMN cars.brand IS 'Car manufacturer (2-50 characters)';
COMMENT ON COLUMN cars.model IS 'Car model (1-50 characters)';
COMMENT ON COLUMN cars.year IS 'Manufacturing year (>= 1950, cannot be future)';
COMMENT ON COLUMN cars.car_class IS 'Car class: ECONOMY, COMFORT, BUSINESS, LUXURY';
COMMENT ON COLUMN cars.price_per_day IS 'Rental price per day in local currency';
COMMENT ON COLUMN cars.user_id IS 'Reference to user-service (OWNER id if owner, RENTER id if rented)';
COMMENT ON COLUMN cars.status IS 'Car status: AVAILABLE, RENTED, MAINTENANCE';
COMMENT ON COLUMN cars.image_url IS 'Optional URL to car image (HTTPS only)';
