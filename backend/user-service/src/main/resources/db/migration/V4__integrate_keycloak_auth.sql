ALTER TABLE users
  ADD COLUMN keycloak_id VARCHAR(36);

UPDATE users
SET keycloak_id = gen_random_uuid()::varchar
WHERE keycloak_id IS NULL;

ALTER TABLE users
  ALTER COLUMN keycloak_id SET NOT NULL;

ALTER TABLE users
  ADD CONSTRAINT uk_users_keycloak_id UNIQUE (keycloak_id);

CREATE INDEX idx_users_keycloak_id ON users (keycloak_id);

ALTER TABLE users
DROP
COLUMN password;

     COMMENT
ON COLUMN users.keycloak_id IS 'Unique ID (sub) from Keycloak server';
