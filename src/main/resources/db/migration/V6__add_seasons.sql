CREATE TABLE season
(
    id          SERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

ALTER TABLE sessions
    ADD COLUMN season_id INT;

ALTER TABLE sessions
    ADD CONSTRAINT fk_sessions_season
        FOREIGN KEY (season_id)
            REFERENCES season (id);