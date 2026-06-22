CREATE TABLE cars (
                    id BIGSERIAL PRIMARY KEY,

                    brand VARCHAR(50) NOT NULL,
                    model VARCHAR(50) NOT NULL,

                    "year" INTEGER NOT NULL CHECK ("year" >= 1950),

                    car_class VARCHAR(20) NOT NULL
                      CHECK (car_class IN ('ECONOMY', 'COMFORT', 'BUSINESS', 'LUXURY')),

                    price_per_day REAL NOT NULL CHECK (price_per_day > 0),

                    user_id BIGINT NOT NULL,

                    status VARCHAR(20) NOT NULL
                      CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE', 'UNCONFIRMED', 'CANCELED')),

                    image_url VARCHAR(500),

                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP
);
