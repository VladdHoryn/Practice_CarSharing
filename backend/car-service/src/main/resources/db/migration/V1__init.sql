
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

