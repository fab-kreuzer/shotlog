ALTER TABLE season
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE;

-- At most one season may be active at a time.
CREATE UNIQUE INDEX uq_season_active ON season (active) WHERE active = TRUE;

-- Default the newest season to active so imports have a target.
UPDATE season
SET active = TRUE
WHERE id = (SELECT MAX(id) FROM season);
