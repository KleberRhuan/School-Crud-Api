-- V14: Substituir tipo ENUM config.channel por VARCHAR + CHECK constraint

-- 1. Remover default e converter coluna
ALTER TABLE config.notification_outbox
  ALTER COLUMN channel DROP DEFAULT,
  ALTER COLUMN channel TYPE VARCHAR(10) USING channel::text;

-- 2. Garantir valores válidos
ALTER TABLE config.notification_outbox
  DROP CONSTRAINT IF EXISTS chk_outbox_channel;

ALTER TABLE config.notification_outbox
  ADD CONSTRAINT chk_outbox_channel
    CHECK (channel IN ('EMAIL', 'SMS', 'PUSH'));

-- 3. Dropar tipo ENUM se não houver dependências
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_type t
    JOIN pg_depend d ON d.refobjid = t.oid
    WHERE t.typname = 'channel'
      AND d.deptype = 'e'
  ) THEN
    DROP TYPE IF EXISTS config.channel;
  END IF;
END$$; 