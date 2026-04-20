# Data Dictionary - Car Sharing System (PostgreSQL)

## Database Information
- **DBMS:** PostgreSQL (≥ 14)
- **Extensions required:** `pgcrypto` (for `gen_random_uuid()`)
- **Naming Convention:** `snake_case`

---

## Table: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT `gen_random_uuid()` | Unique user identifier |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | User's email address (login credential) |
| `password_hash` | `VARCHAR(255)` | NOT NULL | Hashed password (bcrypt/argon2 recommended) |
| `full_name` | `VARCHAR(100)` | NOT NULL | User's full name |
| `role` | `VARCHAR(20)` | NOT NULL, CHECK (`CLIENT`, `ADMIN`) | Role for RBAC access control |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | Account registration timestamp |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT `TRUE` | Soft delete flag |

---

## Table: `cars`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT `gen_random_uuid()` | Unique car identifier |
| `brand` | `VARCHAR(50)` | NOT NULL | Manufacturer (e.g., Toyota, BMW) |
| `model` | `VARCHAR(50)` | NOT NULL | Model name (e.g., Camry, X5) |
| `year` | `INTEGER` | NOT NULL, CHECK (≥1990, ≤next year) | Manufacturing year |
| `class` | `VARCHAR(30)` | NOT NULL, CHECK (`ECONOMY`, `COMFORT`, `BUSINESS`) | Comfort/price class |
| `price_per_day` | `DECIMAL(10,2)` | NOT NULL, CHECK (>0) | Rental price in local currency |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `AVAILABLE`, CHECK (`AVAILABLE`, `RENTED`, `MAINTENANCE`) | Current availability |
| `image_url` | `VARCHAR(500)` | | URL to car photo/image |

---

## Table: `bookings`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT `gen_random_uuid()` | Unique booking identifier |
| `user_id` | `UUID` | FK → `users(id)`, NOT NULL, ON DELETE `RESTRICT` | Customer who made the booking |
| `car_id` | `UUID` | FK → `cars(id)`, NOT NULL, ON DELETE `RESTRICT` | Car being booked |
| `start_date` | `TIMESTAMP` | NOT NULL | Rental start date and time |
| `end_date` | `TIMESTAMP` | NOT NULL | Rental end date and time |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `PENDING`, CHECK (`PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`) | Booking lifecycle status |
| `total_price` | `DECIMAL(10,2)` | NOT NULL, CHECK (≥0) | Total cost of the rental |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | Booking creation timestamp |

---

## Table: `payments`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT `gen_random_uuid()` | Unique payment identifier |
| `booking_id` | `UUID` | FK → `bookings(id)`, NOT NULL, UNIQUE, ON DELETE `CASCADE` | Associated booking (1:1) |
| `amount` | `DECIMAL(10,2)` | NOT NULL, CHECK (>0) | Payment amount |
| `method` | `VARCHAR(20)` | NOT NULL, CHECK (`CARD`, `CASH`) | Payment method |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `PENDING`, CHECK (`PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`) | Transaction status |
| `transaction_id` | `VARCHAR(255)` | | External payment gateway transaction ID |
| `payment_date` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | Completion timestamp |

---

## Table: `split_access`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT `gen_random_uuid()` | Unique access record identifier |
| `booking_id` | `UUID` | FK → `bookings(id)`, NOT NULL, ON DELETE `CASCADE` | Shared booking reference |
| `user_id` | `UUID` | FK → `users(id)`, NOT NULL, ON DELETE `RESTRICT` | User granted shared access |
| `role` | `VARCHAR(20)` | NOT NULL, CHECK (`PRIMARY_DRIVER`, `CO_DRIVER`) | Driver role in the booking |
| **UNIQUE** | `(booking_id, user_id)` | | Prevents duplicate user in same booking |

---

## Table: `reviews`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT `gen_random_uuid()` | Unique review identifier |
| `user_id` | `UUID` | FK → `users(id)`, NOT NULL, ON DELETE `RESTRICT` | Author of the review |
| `car_id` | `UUID` | FK → `cars(id)`, NOT NULL, ON DELETE `CASCADE` | Car being reviewed |
| `rating` | `INTEGER` | NOT NULL, CHECK (1-5) | Star rating (1 = worst, 5 = best) |
| `comment` | `TEXT` | | Optional text feedback |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | Submission timestamp |

---

## Table: `notifications`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK, NOT NULL, DEFAULT `gen_random_uuid()` | Unique notification identifier |
| `user_id` | `UUID` | FK → `users(id)`, NOT NULL, ON DELETE `CASCADE` | Recipient user |
| `message` | `TEXT` | NOT NULL | Notification content |
| `is_read` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Read status (client-side tracking) |
| `type` | `VARCHAR(30)` | NOT NULL, CHECK (`BOOKING_STATUS`, `REMINDER`, `PROMO`) | Category for UI handling |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | Creation timestamp |

---

## PostgreSQL Specific Notes

### UUID Generation
- Uses `gen_random_uuid()` from `pgcrypto` extension
- Alternative: `uuid_generate_v4()` from `uuid-ossp` extension
- Run `CREATE EXTENSION IF NOT EXISTS "pgcrypto";` before schema

### Index Strategy
- B-tree indexes on all foreign keys and frequently queried columns
- Composite index on `bookings(start_date, end_date)` for date range queries
- `CONCURRENTLY` flag used for production-safe index creation

### Constraints Strategy
- `CHECK` constraints instead of `ENUM` types (easier to modify later)
- `RESTRICT` vs `CASCADE`: Chosen to preserve business history

### Soft Delete
- `users.is_active` flag instead of physical deletion
- Historical bookings and reviews remain accessible
