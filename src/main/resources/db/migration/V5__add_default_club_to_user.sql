ALTER TABLE users
    ADD COLUMN home_club BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_home_club
        FOREIGN KEY (home_club) REFERENCES shootingplace(id) ON DELETE SET NULL;