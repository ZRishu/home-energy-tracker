CREATE TABLE devices
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255),
    type     VARCHAR(50),
    location VARCHAR(255),
    user_id  BIGINT,

    CONSTRAINT fk_device_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_device_user_id ON devices (user_id);
