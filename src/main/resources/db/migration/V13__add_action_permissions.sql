-- Add fine-grained create/edit/delete permissions for each resource,
-- complementing the existing view permissions (V11/V12).

INSERT INTO permissions (permission_name, description, resource, action)
VALUES ('create_user', 'Create users', 'user', 'create'),
       ('edit_user', 'Edit users', 'user', 'edit'),
       ('delete_user', 'Delete users', 'user', 'delete'),
       ('create_role', 'Create roles', 'role', 'create'),
       ('edit_role', 'Edit roles and their permissions', 'role', 'edit'),
       ('delete_role', 'Delete roles', 'role', 'delete'),
       ('create_club', 'Create clubs/shooting places', 'club', 'create'),
       ('edit_club', 'Edit clubs/shooting places', 'club', 'edit'),
       ('delete_club', 'Delete clubs/shooting places', 'club', 'delete'),
       ('create_team', 'Create teams', 'team', 'create'),
       ('edit_team', 'Edit teams', 'team', 'edit'),
       ('delete_team', 'Delete teams', 'team', 'delete');

-- Grant ADMIN every new permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.action IN ('create', 'edit', 'delete')
WHERE r.name = 'ADMIN';

-- Preserve current behavior: SPORT_LEADER managed teams under the old team tab,
-- so grant it full team management (create/edit/delete) alongside its view access.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.resource = 'team' AND p.action IN ('create', 'edit', 'delete')
WHERE r.name = 'SPORT_LEADER';
