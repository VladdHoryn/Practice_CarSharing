-- =====================================================
-- Payment Service - Foreign keys and indexes
-- Version: V3
-- =====================================================

-- =====================================================
-- 1. INDEXES for performance
-- =====================================================

-- Index for booking_id (частий пошук платежу по бронюванню)
CREATE INDEX idx_payments_booking_id ON payments(booking_id);

-- Composite index for status + payment_date (для звітів)
CREATE INDEX idx_payments_status_date ON payments(status, payment_date);

-- Index for method + status (аналітика популярності методів оплати)
CREATE INDEX idx_payments_method_status ON payments(method, status);

-- Index for amount range queries
CREATE INDEX idx_payments_amount ON payments(amount);

-- Index for created_at (аудит)
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- =====================================================
-- 2. FOREIGN KEY to booking-service
-- =====================================================
-- В мікросервісній архітектурі фізичний FK неможливий,
-- але для цілісності в межах одного сервісу додаємо коментар:
ALTER TABLE payments ADD CONSTRAINT fk_payments_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(id);

-- =====================================================
-- 3. Автоматичне оновлення updated_at
-- =====================================================

-- Додаємо колонку updated_at, якщо її ще немає
ALTER TABLE payments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Функція для оновлення
CREATE OR REPLACE FUNCTION update_payment_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Тригер
DROP TRIGGER IF EXISTS trigger_payment_updated_at ON payments;
CREATE TRIGGER trigger_payment_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_updated_at();

-- =====================================================
-- 4. Додаткове обмеження: сума платежу не може бути NULL
-- =====================================================
ALTER TABLE payments ALTER COLUMN amount SET NOT NULL;
