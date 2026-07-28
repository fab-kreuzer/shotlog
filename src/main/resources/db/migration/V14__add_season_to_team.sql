ALTER TABLE team
    ADD COLUMN season_id INT;

-- Backfill existing teams to the currently active season (fall back to the
-- newest season if none is flagged active) so no team is left without one
-- before the NOT NULL constraint is applied.
UPDATE team
SET season_id = COALESCE(
        (SELECT id FROM season WHERE active = TRUE LIMIT 1),
        (SELECT id FROM season ORDER BY id DESC LIMIT 1)
                 )
WHERE season_id IS NULL;

ALTER TABLE team
    ALTER COLUMN season_id SET NOT NULL;

ALTER TABLE team
    ADD CONSTRAINT fk_team_season
        FOREIGN KEY (season_id)
            REFERENCES season (id);
