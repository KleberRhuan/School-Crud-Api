ALTER TABLE school.school_metrics_jsonb 
ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN created_by BIGINT REFERENCES account.users(id),
ADD COLUMN updated_by BIGINT REFERENCES account.users(id),
ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

-- Comentários
COMMENT ON COLUMN school.school.version IS 'Versão para controle de concorrência otimista';
COMMENT ON COLUMN school.school_metrics_jsonb.created_at IS 'Data de criação (campo Auditable)';
COMMENT ON COLUMN school.school_metrics_jsonb.updated_at IS 'Data da última modificação (campo Auditable)';

