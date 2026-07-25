-- Add resource/action decomposition to permissions, in preparation for
-- fine-grained resource x action permissions (e.g. user:edit, club:create)

ALTER TABLE permissions
    ADD COLUMN resource VARCHAR(50),
    ADD COLUMN action   VARCHAR(50);

UPDATE permissions
SET resource = 'user',
    action   = 'view'
WHERE permission_name = 'view_user_tab';
UPDATE permissions
SET resource = 'role',
    action   = 'view'
WHERE permission_name = 'view_role_tab';
UPDATE permissions
SET resource = 'club',
    action   = 'view'
WHERE permission_name = 'view_club_tab';
UPDATE permissions
SET resource = 'team',
    action   = 'view'
WHERE permission_name = 'view_team_tab';

ALTER TABLE permissions
    ALTER COLUMN resource SET NOT NULL,
    ALTER COLUMN action SET NOT NULL;
