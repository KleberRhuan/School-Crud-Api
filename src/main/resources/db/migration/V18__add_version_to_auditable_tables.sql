ALTER TABLE school.school_metrics_jsonb 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES account.users(id),
ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES account.users(id),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- Comentários
COMMENT ON COLUMN school.school_metrics_jsonb.created_at IS 'Data de criação (campo Auditable)';
COMMENT ON COLUMN school.school_metrics_jsonb.updated_at IS 'Data da última modificação (campo Auditable)';

