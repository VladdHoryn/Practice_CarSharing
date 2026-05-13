-- =====================================================
-- User Service - Add indexes and constraints
-- Version: V3
-- =====================================================

-- Composite index for role + active status (частий запит: active users by role)
CREATE INDEX idx_users_role_active ON users(role, is_active);

-- Index for full_name search (пошук по імені)
CREATE INDEX idx_users_full_name ON users(full_name);
