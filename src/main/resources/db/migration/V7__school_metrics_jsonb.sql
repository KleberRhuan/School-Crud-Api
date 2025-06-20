-- Migration V7: Criação da tabela school_metrics_jsonb
-- Responsável por armazenar as métricas das escolas em formato JSONB

CREATE TABLE IF NOT EXISTS school.school_metrics_jsonb (
  school_code   bigserial PRIMARY KEY REFERENCES school.school(code) ON DELETE CASCADE,
  metrics       JSONB NOT NULL,
  
  -- Colunas de auditoria (Auditable)
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by    BIGINT,
  updated_by    BIGINT NOT NULL
);

-- Índices para performance em consultas JSONB
CREATE INDEX IF NOT EXISTS idx_metrics_gin ON school.school_metrics_jsonb
USING gin (metrics jsonb_path_ops);

-- Comentários das colunas
COMMENT ON TABLE school.school_metrics_jsonb IS 'Métricas das escolas em formato JSONB';
COMMENT ON COLUMN school.school_metrics_jsonb.school_code IS 'Código da escola (FK)';
COMMENT ON COLUMN school.school_metrics_jsonb.metrics IS 'Métricas em formato JSONB {metrica: valor}';