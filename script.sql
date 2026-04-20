-- =====================================================
-- Car Sharing System - Database Schema (PostgreSQL)
-- =====================================================

-- Enable UUID extension (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- 1. Users table
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CLIENT', 'ADMIN')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =====================================================
-- 2. Cars table
-- =====================================================
CREATE TABLE cars (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL CHECK (year >= 1990 AND year <= EXTRACT(YEAR FROM NOW()) + 1),
    class VARCHAR(30) NOT NULL CHECK (class IN ('ECONOMY', 'COMFORT', 'BUSINESS')),
    price_per_day DECIMAL(10,2) NOT NULL CHECK (price_per_day > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE')),
    image_url VARCHAR(500)
);

-- =====================================================
-- 3. Bookings table
-- =====================================================
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    car_id UUID NOT NULL REFERENCES cars(id) ON DELETE RESTRICT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (end_date > start_date)
);

-- =====================================================
-- 4. Payments table
-- =====================================================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method VARCHAR(20) NOT NULL CHECK (method IN ('CARD', 'CASH')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================
-- 5. Split Access table (many-to-many between bookings and users)
-- =====================================================
CREATE TABLE split_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PRIMARY_DRIVER', 'CO_DRIVER')),
    UNIQUE(booking_id, user_id)
);

-- =====================================================
-- 6. Reviews table
-- =====================================================
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    car_id UUID NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================
-- 7. Notifications table
-- =====================================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('BOOKING_STATUS', 'REMINDER', 'PROMO')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================
-- Indexes for performance (PostgreSQL specific)
-- =====================================================

-- Users indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_role ON users(role);

-- Cars indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_cars_status ON cars(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_cars_class ON cars(class);

-- Bookings indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_bookings_car_id ON bookings(car_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_bookings_date_range ON bookings(start_date, end_date);

-- Payments indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_status ON payments(status);

-- Split access indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_split_access_booking_id ON split_access(booking_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_split_access_user_id ON split_access(user_id);

-- Reviews indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reviews_car_id ON reviews(car_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);

-- Notifications indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

-- =====================================================
-- Comments on tables and columns (documentation)
-- =====================================================

COMMENT ON TABLE users IS 'System users with authentication and role-based access';
COMMENT ON COLUMN users.role IS 'User role: CLIENT (regular customer) or ADMIN (system administrator)';
COMMENT ON COLUMN users.is_active IS 'Soft delete flag - false means account is deactivated';

COMMENT ON COLUMN cars.class IS 'Car class: ECONOMY, COMFORT, or BUSINESS';
COMMENT ON COLUMN cars.status IS 'Current car status: AVAILABLE (free), RENTED (in use), MAINTENANCE (unavailable)';

COMMENT ON TABLE bookings IS 'Car booking requests made by users';

COMMENT ON TABLE payments IS 'Payments for bookings, one-to-one relation';
COMMENT ON COLUMN payments.status IS 'Payment status: PENDING, SUCCESS, FAILED, REFUNDED';

COMMENT ON TABLE split_access IS 'Shared access for multiple drivers to the same booking (killer feature)';
COMMENT ON COLUMN split_access.role IS 'Driver role: PRIMARY_DRIVER (main responsible) or CO_DRIVER (additional driver)';

COMMENT ON COLUMN reviews.rating IS 'Rating from 1 (worst) to 5 (best)';

COMMENT ON TABLE notifications IS 'User notifications about booking status, reminders, etc.';
COMMENT ON COLUMN notifications.type IS 'Notification type: BOOKING_STATUS, REMINDER, PROMO';
