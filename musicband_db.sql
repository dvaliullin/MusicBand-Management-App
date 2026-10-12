DO $$ 
BEGIN
CREATE TYPE musicgenre AS ENUM (
    'PSYCHEDELIC_CLOUD_RAP',
    'POP',
    'POST_ROCK'
);
EXCEPTION
WHEN duplicate_object THEN null;
END $$;

CREATE OR REPLACE FUNCTION delete_label_cascade()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM label 
    WHERE id = OLD.label_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_reverse_cascade
AFTER DELETE ON music_band
FOR EACH ROW
EXECUTE FUNCTION delete_label_cascade();

BEGIN;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS label (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    bands BIGINT CHECK(bands >= 0)
);

CREATE TABLE IF NOT EXISTS music_band (
    id SERIAL PRIMARY KEY,
    key INTEGER UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL CHECK(length(name) > 0),
    x INTEGER,
    y BIGINT NOT NULL,
    creation_date TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    number_of_participants INTEGER CHECK(number_of_participants > 0),
    genre MUSICGENRE,
    label_id INTEGER REFERENCES label(id) ON DELETE CASCADE,
    owner_id INTEGER REFERENCES users(id) ON DELETE CASCADE
);

END;