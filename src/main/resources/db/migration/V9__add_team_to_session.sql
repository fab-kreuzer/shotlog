ALTER TABLE sessions
    ADD COLUMN team_id INT;

ALTER TABLE sessions
    ADD CONSTRAINT fk_sessions_team
        FOREIGN KEY (team_id)
            REFERENCES team (id);
