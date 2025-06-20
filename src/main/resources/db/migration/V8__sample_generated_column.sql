-- Migration V8: coluna gerada para métricas populares


ALTER TABLE school.school_metrics_jsonb
  ADD COLUMN IF NOT EXISTS salas_aula INTEGER
    GENERATED ALWAYS AS ((metrics->>'SALAS_AULA')::integer) STORED;

ALTER TABLE school.school_metrics_jsonb
  ADD COLUMN IF NOT EXISTS salas_utilizadas INTEGER
    GENERATED ALWAYS AS ((metrics->>'SALAS_UTILIZADAS')::integer) STORED;

-- Índices para as colunas geradas
CREATE INDEX IF NOT EXISTS idx_salas_aula ON school.school_metrics_jsonb (salas_aula);
CREATE INDEX IF NOT EXISTS idx_salas_utilizadas ON school.school_metrics_jsonb (salas_utilizadas);

-- Comentários
COMMENT ON COLUMN school.school_metrics_jsonb.salas_aula IS 'Coluna gerada: quantidade de salas de aula (extraído do JSONB)';
COMMENT ON COLUMN school.school_metrics_jsonb.salas_utilizadas IS 'Coluna gerada: quantidade de salas utilizadas (extraído do JSONB)'; 