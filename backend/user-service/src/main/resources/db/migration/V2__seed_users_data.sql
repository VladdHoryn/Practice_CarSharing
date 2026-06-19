INSERT INTO users (keycloak_id, full_name, email, role, is_active)
VALUES
  ('10000000-0000-0000-0000-000000000001', 'System Administrator', 'admin@carsharing.com', 'ADMINISTRATOR', true),

  ('20000000-0000-0000-0000-000000000001', 'John Smith', 'owner1@carsharing.com', 'OWNER', true),
  ('20000000-0000-0000-0000-000000000002', 'Maria Garcia', 'owner2@carsharing.com', 'OWNER', true),

  ('30000000-0000-0000-0000-000000000001', 'James Wilson', 'renter1@carsharing.com', 'RENTER', true),
  ('30000000-0000-0000-0000-000000000002', 'Emily Brown', 'renter2@carsharing.com', 'RENTER', true),
  ('30000000-0000-0000-0000-000000000003', 'Michael Lee', 'renter3@carsharing.com', 'RENTER', true),
  ('30000000-0000-0000-0000-000000000004', 'Sarah Johnson', 'renter4@carsharing.com', 'RENTER', true)
  ON CONFLICT (email) DO NOTHING;

DO
$$
DECLARE
user_count INTEGER;
BEGIN
SELECT COUNT(*)
INTO user_count
FROM users;
RAISE
NOTICE '==========================================';
    RAISE
NOTICE 'Users seeded: % records', user_count;
    RAISE
NOTICE '==========================================';
END $$;

COMMENT
ON TABLE users IS 'Users seeded: 7 records (1 ADMIN, 2 OWNERS, 4 RENTERS)';
