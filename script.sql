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
-- 2. Profiles table (1:1 with users)
-- =====================================================
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    phone VARCHAR(20),
    address TEXT,
    preferences JSONB
);

-- =====================================================
-- 3. Rental Companies table
-- =====================================================
CREATE TABLE rental_companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    address TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20)
);

-- =====================================================
-- 4. Cars table
-- =====================================================
CREATE TABLE cars (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL CHECK (year >= 1990 AND year <= EXTRACT(YEAR FROM NOW()) + 1),
    class VARCHAR(30) NOT NULL CHECK (class IN ('ECONOMY', 'COMFORT', 'BUSINESS')),
    price_per_day DECIMAL(10,2) NOT NULL CHECK (price_per_day > 0),
    rental_company_id UUID NOT NULL REFERENCES rental_companies(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE')),
    image_url VARCHAR(500)
);

-- =====================================================
-- 5. Bookings table
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
-- 6. Payments table
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
-- 7. Split Access table (many-to-many between bookings and users)
-- =====================================================
CREATE TABLE split_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PRIMARY_DRIVER', 'CO_DRIVER')),
    UNIQUE(booking_id, user_id)
);

-- =====================================================
-- 8. Reviews table
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
-- 9. Notifications table
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
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_cars_rental_company_id ON cars(rental_company_id);
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

COMMENT ON TABLE profiles IS 'Extended user profile information (1:1 relation with users)';
COMMENT ON COLUMN profiles.preferences IS 'JSONB field for storing user preferences (notifications, language, etc.)';

COMMENT ON TABLE rental_companies IS 'Car rental companies that provide vehicles to the platform';

COMMENT ON TABLE cars IS 'Cars available for rent, owned by rental companies';
COMMENT ON COLUMN cars.class IS 'Car class: ECONOMY, COMFORT, or BUSINESS';
COMMENT ON COLUMN cars.status IS 'Current car status: AVAILABLE (free), RENTED (in use), MAINTENANCE (unavailable)';

COMMENT ON TABLE bookings IS 'Car booking requests made by users';
COMMENT ON COLUMN bookings.status IS 'Booking status: PENDING (waiting), CONFIRMED (approved), CANCELLED (by user), COMPLETED (rental finished)';

COMMENT ON TABLE payments IS 'Payments for bookings, one-to-one relation';
COMMENT ON COLUMN payments.status IS 'Payment status: PENDING, SUCCESS, FAILED, REFUNDED';

COMMENT ON TABLE split_access IS 'Shared access for multiple drivers to the same booking (killer feature)';
COMMENT ON COLUMN split_access.role IS 'Driver role: PRIMARY_DRIVER (main responsible) or CO_DRIVER (additional driver)';

COMMENT ON TABLE reviews IS 'User reviews and ratings for cars after completed rentals';
COMMENT ON COLUMN reviews.rating IS 'Rating from 1 (worst) to 5 (best)';

COMMENT ON TABLE notifications IS 'User notifications about booking status, reminders, etc.';
COMMENT ON COLUMN notifications.type IS 'Notification type: BOOKING_STATUS, REMINDER, PROMO';
