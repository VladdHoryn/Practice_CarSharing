-- =====================================================
-- User Service - Seed users data
-- Version: V2
-- =====================================================

-- Password: pass123
INSERT INTO users (full_name, password, email, role, is_active)
VALUES
  -- ADMINISTRATOR (id: 1)
  ('System Administrator', '$2a$10$QVke9/kEeFLwFkXSHSANc.VGk3EVxB26yhyirCoCVT5/2O.0dadnW', 'admin@carsharing.com',
   'ADMINISTRATOR', true),

  -- OWNERS (власники авто) (id: 2, 3)
  ('John Smith', '$2a$10$QVke9/kEeFLwFkXSHSANc.VGk3EVxB26yhyirCoCVT5/2O.0dadnW', 'owner1@carsharing.com', 'OWNER',
   true),
  ('Maria Garcia', '$2a$10$QVke9/kEeFLwFkXSHSANc.VGk3EVxB26yhyirCoCVT5/2O.0dadnW', 'owner2@carsharing.com', 'OWNER',
   true),

  -- RENTERS (орендарі) (id: 4, 5, 6, 7)
  ('James Wilson', '$2a$10$QVke9/kEeFLwFkXSHSANc.VGk3EVxB26yhyirCoCVT5/2O.0dadnW', 'renter1@carsharing.com', 'RENTER',
   true),
  ('Emily Brown', '$2a$10$QVke9/kEeFLwFkXSHSANc.VGk3EVxB26yhyirCoCVT5/2O.0dadnW', 'renter2@carsharing.com', 'RENTER',
   true),
  ('Michael Lee', '$2a$10$QVke9/kEeFLwFkXSHSANc.VGk3EVxB26yhyirCoCVT5/2O.0dadnW', 'renter3@carsharing.com', 'RENTER',
   true),
  ('Sarah Johnson', '$2a$10$QVke9/kEeFLwFkXSHSANc.VGk3EVxB26yhyirCoCVT5/2O.0dadnW', 'renter4@carsharing.com', 'RENTER',
   true)
ON CONFLICT (email) DO NOTHING;

-- Verify
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
