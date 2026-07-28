-- Season management permissions, mirroring the team permission set
-- (view_team_tab + create/edit/delete_team from V11/V12/V13).

INSERT INTO permissions (permission_name, description, resource, action)
VALUES ('view_season_tab', 'View and manage seasons', 'season', 'view'),
       ('create_season', 'Create seasons', 'season', 'create'),
       ('edit_season', 'Edit seasons', 'season', 'edit'),
       ('delete_season', 'Delete seasons', 'season', 'delete');

-- Grant ADMIN the new season permissions (explicit rows — nothing is automatic).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.resource = 'season'
WHERE r.name = 'ADMIN';
