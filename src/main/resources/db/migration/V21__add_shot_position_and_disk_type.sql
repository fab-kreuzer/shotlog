-- Shot positions (1/100 mm from target centre, +x right, +y up) and target disk type for scanned results.
ALTER TABLE shot ADD COLUMN IF NOT EXISTS x INTEGER;
ALTER TABLE shot ADD COLUMN IF NOT EXISTS y INTEGER;
ALTER TABLE sessions ADD COLUMN IF NOT EXISTS disk_type VARCHAR(16);
