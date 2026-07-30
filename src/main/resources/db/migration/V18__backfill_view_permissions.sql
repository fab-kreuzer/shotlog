-- Backfill the view_*_tab permissions (missing from some environments) and
-- make sure ADMIN has every permission that currently exists.

INSERT INTO permissions (permission_name, description, resource, action)
VALUES ('view_user_tab', 'View and manage users (create, edit, delete, assign roles)', 'user', 'view'),
       ('view_role_tab', 'View and manage roles and their permissions', 'role', 'view'),
       ('view_club_tab', 'View and manage clubs/shooting places', 'club', 'view'),
       ('view_team_tab', 'View and manage teams', 'team', 'view'),
       ('view_season_tab', 'View and manage seasons', 'season', 'view')
ON CONFLICT (permission_name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
