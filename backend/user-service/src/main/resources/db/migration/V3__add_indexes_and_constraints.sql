-- =====================================================
-- User Service - Add indexes and constraints
-- Version: V3
-- =====================================================

-- Composite index for role + active status (частий запит: active users by role)
CREATE INDEX idx_users_role_active ON users(role, is_active);

-- Index for full_name search (пошук по імені)
CREATE INDEX idx_users_full_name ON users(full_name);

-- Partial index for active users only (оптимізація для активних користувачів)
CREATE INDEX idx_users_active_only ON users(id) WHERE is_active = true;

-- Index for created_at range queries (звіти, статистика)
CREATE INDEX idx_users_created_at ON users(created_at);

COMMENT ON INDEX idx_users_role_active IS 'Optimizes queries filtering by role and active status';
COMMENT ON INDEX idx_users_full_name IS 'Optimizes search by user full name';
COMMENT ON INDEX idx_users_active_only IS 'Partial index for active users only';
COMMENT ON INDEX idx_users_created_at IS 'Optimizes date range queries for reports';
