-- V13: Substituir tipo ENUM audit.actor_type por VARCHAR + CHECK constraint

-- 1. Remover default e converter para texto
ALTER TABLE audit.audit_event
  ALTER COLUMN actor_type DROP DEFAULT,
  ALTER COLUMN actor_type TYPE VARCHAR(10) USING actor_type::text;

-- 2. Remove constraint anterior, se existir, e cria nova
ALTER TABLE audit.audit_event
  DROP CONSTRAINT IF EXISTS chk_audit_actor_type;

ALTER TABLE audit.audit_event
  ADD CONSTRAINT chk_audit_actor_type
    CHECK (actor_type IN ('USER', 'SYSTEM'));

-- 3. Opcional: remover tipo ENUM se não houver dependências restantes
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_type t
      JOIN pg_depend d ON d.refobjid = t.oid
    WHERE t.typname = 'actor_type'
      AND d.deptype = 'e'
  ) THEN
    DROP TYPE IF EXISTS audit.actor_type;
  END IF;
END$$; 