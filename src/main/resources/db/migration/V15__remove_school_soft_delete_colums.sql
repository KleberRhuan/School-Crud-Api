ALTER TABLE school.school
    DROP COLUMN IF EXISTS deleted_at,
    DROP COLUMN IF EXISTS deleted;