ALTER TABLE account.password_reset
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NULL,
    DROP CONSTRAINT IF EXISTS fk_password_reset_created_by,
    DROP CONSTRAINT IF EXISTS fk_password_reset_updated_by;

ALTER TABLE account.password_reset
    ADD CONSTRAINT fk_password_reset_created_by
        FOREIGN KEY (created_by) REFERENCES account.users(id) ON DELETE SET NULL;

ALTER TABLE account.password_reset
    ADD CONSTRAINT fk_password_reset_updated_by
        FOREIGN KEY (updated_by) REFERENCES account.users(id) ON DELETE SET NULL;

