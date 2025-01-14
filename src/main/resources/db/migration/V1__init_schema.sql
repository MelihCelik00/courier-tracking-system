CREATE TABLE courier (
    id BIGSERIAL PRIMARY KEY,
    total_travel_distance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_latitude DOUBLE PRECISION,
    last_longitude DOUBLE PRECISION,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE store (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL
);

CREATE TABLE store_entry (
    id BIGSERIAL PRIMARY KEY,
    courier_id BIGINT NOT NULL REFERENCES courier(id),
    store_id BIGINT NOT NULL REFERENCES store(id),
    entry_time TIMESTAMP WITH TIME ZONE NOT NULL,
    entry_latitude DOUBLE PRECISION NOT NULL,
    entry_longitude DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_courier FOREIGN KEY (courier_id) REFERENCES courier(id),
    CONSTRAINT fk_store FOREIGN KEY (store_id) REFERENCES store(id)
);

CREATE INDEX idx_store_entry_courier_store ON store_entry(courier_id, store_id);
CREATE INDEX idx_store_entry_time ON store_entry(entry_time); 