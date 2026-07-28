-- Move profile pictures out of the users table into a dedicated table, so the
-- blob is no longer loaded with the user (which rides on the session principal).
CREATE TABLE user_avatars (
    user_id      BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    data         BYTEA NOT NULL,
    content_type VARCHAR(100) NOT NULL
);

INSERT INTO user_avatars (user_id, data, content_type)
SELECT id, avatar_data, COALESCE(avatar_type, 'image/png')
FROM users
WHERE avatar_data IS NOT NULL;

ALTER TABLE users DROP COLUMN avatar_data;
ALTER TABLE users DROP COLUMN avatar_type;
