ALTER TABLE users
  ADD COLUMN driver_code VARCHAR(10);

UPDATE users
SET driver_code = CASE email
                    WHEN 'admin@carsharing.com'   THEN 'ADM7KX92QP'
                    WHEN 'owner1@carsharing.com'  THEN 'OWN4TR85LM'
                    WHEN 'owner2@carsharing.com'  THEN 'OWN9YU37NB'
                    WHEN 'renter1@carsharing.com' THEN 'RNT2GH68JK'
                    WHEN 'renter2@carsharing.com' THEN 'RNT5PL91ZX'
                    WHEN 'renter3@carsharing.com' THEN 'RNT8CV24DF'
                    WHEN 'renter4@carsharing.com' THEN 'RNT1MN73QA'
  END
WHERE driver_code IS NULL;

ALTER TABLE users
  ALTER COLUMN driver_code SET NOT NULL;

ALTER TABLE users
  ADD CONSTRAINT uk_users_driver_code UNIQUE (driver_code);

CREATE INDEX idx_users_driver_code
  ON users(driver_code);

COMMENT ON COLUMN users.driver_code IS
'Unique driver invitation code used for adding additional drivers to bookings';

        DO
$$
DECLARE
missing_codes INTEGER;
BEGIN
SELECT COUNT(*)
INTO missing_codes
FROM users
WHERE driver_code IS NULL;

IF missing_codes > 0 THEN
        RAISE EXCEPTION 'Some users have NULL driver_code values';
END IF;

    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Driver codes successfully assigned';
    RAISE NOTICE '==========================================';
END
$$;
