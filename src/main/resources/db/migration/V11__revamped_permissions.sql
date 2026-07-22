-- Revamped permission system: fine-grained permissions grouped under roles

-- Create permissions table
CREATE TABLE permissions
(
    id              BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description     VARCHAR(255)
);

-- Create role_permissions join table
CREATE TABLE role_permissions
(
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- Seed permissions matching the current admin-only settings tabs
INSERT INTO permissions (permission_name, description)
VALUES ('view_user_tab', 'View and manage users (create, edit, delete, assign roles)'),
       ('view_role_tab', 'View and manage roles and their permissions'),
       ('view_club_tab', 'View and manage clubs/shooting places'),
       ('view_team_tab', 'View and manage teams');

-- Grant ADMIN every permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- Preserve current behavior: SPORT_LEADER (if it exists as a role) also sees the team tab
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.permission_name = 'view_team_tab'
WHERE r.name = 'SPORT_LEADER';
