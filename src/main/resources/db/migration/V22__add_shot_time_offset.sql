-- Seconds since the previous shot (DISAG "offset"; 0 for the first shot), only set for scanned results.
ALTER TABLE shot ADD COLUMN IF NOT EXISTS seconds_since_previous INTEGER;
