-- Rename the column shooting_place_id to enemy_id
ALTER TABLE sessions RENAME COLUMN shooting_place_id TO enemy_id;

-- Add a new column isHome
ALTER TABLE sessions ADD COLUMN is_home BOOLEAN NOT NULL DEFAULT FALSE;