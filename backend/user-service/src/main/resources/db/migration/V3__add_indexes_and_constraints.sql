CREATE INDEX idx_users_role_active ON users(role, is_active);

CREATE INDEX idx_users_full_name ON users(full_name);
