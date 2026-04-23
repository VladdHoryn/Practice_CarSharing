-- =====================================================
-- BOOKING SERVICE - Initial Schema Migration
-- =====================================================

-- =====================================================
-- ENUMS
-- =====================================================

CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REJECTED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');

-- =====================================================
-- TABLE: bookings
-- =====================================================

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    renter_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status booking_status NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (end_date > start_date)
);

-- =====================================================
-- TABLE: payments
-- =====================================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    status payment_status NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: split_access (killer feature)
-- =====================================================

CREATE TABLE split_access (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PRIMARY_DRIVER', 'CO_DRIVER')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_split_access_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT unique_booking_user UNIQUE (booking_id, user_id)
);

-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_bookings_renter_id ON bookings(renter_id);
CREATE INDEX idx_bookings_car_id ON bookings(car_id);
CREATE INDEX idx_bookings_owner_id ON bookings(owner_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_date_range ON bookings(start_date, end_date);
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_split_access_booking_id ON split_access(booking_id);
CREATE INDEX idx_split_access_user_id ON split_access(user_id);

-- =====================================================
-- SEED DATA
-- =====================================================

-- Insert a sample booking (IDs 1, 2, 3 correspond to users from user-service)
INSERT INTO bookings (renter_id, car_id, owner_id, start_date, end_date, status, total_price)
VALUES
  (1, 1, 2, NOW() + INTERVAL '2 days', NOW() + INTERVAL '5 days', 'CONFIRMED', 150.00),
  (2, 2, 1, NOW() + INTERVAL '1 day', NOW() + INTERVAL '3 days', 'PENDING', 240.00);

-- Insert a sample payment for the first booking
INSERT INTO payments (booking_id, amount, status, transaction_id)
VALUES
  (1, 150.00, 'SUCCESS', 'txn_123456789');

-- Insert a sample split access (co-driver for first booking)
INSERT INTO split_access (booking_id, user_id, role)
VALUES
  (1, 2, 'CO_DRIVER');
