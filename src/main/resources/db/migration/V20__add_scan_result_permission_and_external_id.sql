-- QR scan import: permission to scan DISAG results, plus an external id on sessions for de-duplication.

INSERT INTO permissions (permission_name, description, resource, action)
VALUES ('scan_result', 'Import results by scanning a DISAG QR code', 'session', 'scan')
ON CONFLICT (permission_name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE p.permission_name = 'scan_result'
ON CONFLICT (role_id, permission_id) DO NOTHING;

ALTER TABLE sessions ADD COLUMN IF NOT EXISTS external_id VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uq_sessions_user_external_id
    ON sessions (user_id, external_id) WHERE external_id IS NOT NULL;
