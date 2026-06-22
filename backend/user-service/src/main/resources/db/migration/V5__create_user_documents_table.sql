CREATE TABLE user_documents (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              document_type VARCHAR(50) NOT NULL,
                              file_data BYTEA NOT NULL,
                              content_type VARCHAR(100) NOT NULL,
                              original_file_name VARCHAR(255),
                              is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                              uploaded_at TIMESTAMP NOT NULL,

                              CONSTRAINT fk_user_documents_user_id
                                FOREIGN KEY (user_id)
                                  REFERENCES users (id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_user_documents_user_id ON user_documents(user_id);
