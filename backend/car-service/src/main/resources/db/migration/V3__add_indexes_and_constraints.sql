-- =====================================================
-- Car Service - Indexes, constraints and foreign keys
-- Version: V3
-- =====================================================

-- =====================================================
-- 1. FOREIGN KEY to user-service
-- =====================================================
-- Зверніть увагу: REFERENCES users(id) - це посилання на user-service
-- В мікросервісній архітектурі цей FK не може бути фізичним,
-- тому він залишається як логічне посилання.
-- Для демонстрації залишаємо коментар:
ALTER TABLE cars ADD CONSTRAINT fk_cars_user_id FOREIGN KEY (user_id) REFERENCES users(id);

-- =====================================================
-- 2. Indexes for performance
-- =====================================================

-- Composite index for owner + status (частий запит: показати всі авто власника з певним статусом)
CREATE INDEX idx_cars_user_status ON cars(user_id, status);

-- Index for price range queries
CREATE INDEX idx_cars_price_per_day ON cars(price_per_day);

-- Index for car class + price (фільтрація по класу та ціні)
CREATE INDEX idx_cars_class_price ON cars(car_class, price_per_day);

-- Index for brand searches
CREATE INDEX idx_cars_brand ON cars(brand);

-- Index for year range queries
CREATE INDEX idx_cars_year ON cars(year);
