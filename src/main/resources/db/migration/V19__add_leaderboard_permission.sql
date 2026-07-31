-- Leaderboard feature: view-only permission for the per-team leaderboard page.

INSERT INTO permissions (permission_name, description, resource, action)
VALUES ('view_leaderboard', 'View team leaderboards', 'leaderboard', 'view')
ON CONFLICT (permission_name) DO NOTHING;

-- Grant it to every role that already exists (ADMIN, USER, and SPORT_LEADER if present) —
-- the leaderboard is a general engagement feature, not an admin-only one.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE p.permission_name = 'view_leaderboard'
ON CONFLICT (role_id, permission_id) DO NOTHING;
