-- Migration V9: Tabelas para controle de importação CSV
-- Inclui tabelas do Spring Batch e controle de jobs

CREATE SCHEMA IF NOT EXISTS csv;

-- Tabela para controlar importações
CREATE TABLE IF NOT EXISTS csv.csv_import_job (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  filename      TEXT NOT NULL,
  status        TEXT NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
  total_records INTEGER DEFAULT 0,
  processed_records INTEGER DEFAULT 0,
  error_records INTEGER DEFAULT 0,
  error_message TEXT,
  started_at    TIMESTAMP,
  finished_at   TIMESTAMP,
  
  -- Colunas de auditoria (Auditable)
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by    BIGINT NOT NULL,
  updated_by    BIGINT NOT NULL
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_csv_import_status ON csv.csv_import_job(status);
CREATE INDEX IF NOT EXISTS idx_csv_import_created_by ON csv.csv_import_job(created_by);
CREATE INDEX IF NOT EXISTS idx_csv_import_created_at ON csv.csv_import_job(created_at);

-- Comentários
COMMENT ON TABLE csv.csv_import_job IS 'Controle de jobs de importação CSV';
COMMENT ON COLUMN csv.csv_import_job.id IS 'ID único do job';
COMMENT ON COLUMN csv.csv_import_job.filename IS 'Nome do arquivo CSV importado';
COMMENT ON COLUMN csv.csv_import_job.status IS 'Status do job (PENDING, RUNNING, COMPLETED, FAILED)';
COMMENT ON COLUMN csv.csv_import_job.total_records IS 'Total de registros no CSV';
COMMENT ON COLUMN csv.csv_import_job.processed_records IS 'Registros processados com sucesso';
COMMENT ON COLUMN csv.csv_import_job.error_records IS 'Registros com erro';
COMMENT ON COLUMN csv.csv_import_job.error_message IS 'Mensagem de erro se houver falha';
COMMENT ON COLUMN csv.csv_import_job.created_by IS 'Usuário que iniciou a importação (usando auditoria)'; 