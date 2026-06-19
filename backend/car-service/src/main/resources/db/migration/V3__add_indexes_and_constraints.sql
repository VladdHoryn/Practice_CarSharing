CREATE INDEX idx_cars_user_status ON cars(user_id, status);

CREATE INDEX idx_cars_price_per_day ON cars(price_per_day);

CREATE INDEX idx_cars_class_price ON cars(car_class, price_per_day);

CREATE INDEX idx_cars_brand ON cars(brand);

CREATE INDEX idx_cars_year ON cars(year);
