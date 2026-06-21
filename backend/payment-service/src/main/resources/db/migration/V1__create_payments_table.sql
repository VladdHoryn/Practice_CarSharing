CREATE TYPE payment_method_enum AS ENUM (
    'CARD',
    'GOOGLE_PAY',
    'APPLE_PAY'
);

CREATE TYPE payment_status_enum AS ENUM (
    'CREATED',
    'PENDING',
    'PROCESSING',
    'SUCCESS',
    'FAILED',
    'CANCELLED',
    'REFUNDED'
);

CREATE TABLE payments
(
    id                  BIGSERIAL PRIMARY KEY,
    booking_id          BIGINT              NOT NULL,
    amount              DECIMAL(10, 2)      NOT NULL CHECK (amount > 0),
    method              payment_method_enum NOT NULL,
    status              payment_status_enum NOT NULL DEFAULT 'CREATED',
    payment_date        TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider_payment_id VARCHAR(255),
    currency            VARCHAR(10)         NOT NULL,
    idempotency_key     VARCHAR(255) UNIQUE,
    client_secret       VARCHAR(255),
    created_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);
