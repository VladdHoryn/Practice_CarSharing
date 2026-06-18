CREATE TABLE car_images (
                          id BIGSERIAL PRIMARY KEY,

                          car_id BIGINT NOT NULL,

                          image_data BYTEA NOT NULL,

                          content_type VARCHAR(100) NOT NULL,
                          file_name VARCHAR(255) NOT NULL,
                          file_size BIGINT NOT NULL,
                          is_main BOOLEAN NOT NULL DEFAULT FALSE,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_car_images_car_id
                            FOREIGN KEY (car_id)
                              REFERENCES cars (id)
                              ON DELETE CASCADE
);

ALTER TABLE cars DROP COLUMN image_url;
