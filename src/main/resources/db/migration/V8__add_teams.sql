CREATE TABLE team
(
    id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE user_team
(
    user_id INT NOT NULL,
    team_id INT NOT NULL,
    role    VARCHAR(50) DEFAULT 'MEMBER',

    PRIMARY KEY (user_id, team_id),

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE CASCADE
);