-- =====================================================
-- ENUMS
-- =====================================================

CREATE TYPE car_status AS ENUM ('AVAILABLE', 'RENTED', 'MAINTENANCE');

CREATE TYPE car_class AS ENUM ('ECONOMY', 'COMFORT', 'BUSINESS', 'LUXURY');


-- =====================================================
-- TABLE
-- =====================================================

CREATE TABLE cars (
                    id BIGSERIAL PRIMARY KEY,

                    brand VARCHAR(50) NOT NULL,
                    model VARCHAR(50) NOT NULL,

                    year INTEGER NOT NULL CHECK (year >= 1950),

    car_class car_class NOT NULL,

    price_per_day REAL NOT NULL CHECK (price_per_day > 0),

    user_id BIGINT NOT NULL,

    status car_status NOT NULL,

    image_url VARCHAR(500)
);


-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_cars_user_id ON cars(user_id);
CREATE INDEX idx_cars_status ON cars(status);


-- =====================================================
-- SEED DATA
-- =====================================================

INSERT INTO cars (brand, model, year, car_class, price_per_day, user_id, status, image_url)
VALUES
  ('Toyota', 'Camry', 2023, 'COMFORT', 50.0, 1, 'AVAILABLE', 'https://example.com/camry.jpg'),
  ('BMW', 'X5', 2022, 'LUXURY', 120.0, 2, 'AVAILABLE', 'https://example.com/bmw.jpg'),
  ('Hyundai', 'Accent', 2021, 'ECONOMY', 25.0, 3, 'AVAILABLE', NULL),
  ('Tesla', 'Model 3', 2024, 'BUSINESS', 150.0, 4, 'AVAILABLE', 'https://example.com/tesla.jpg');
