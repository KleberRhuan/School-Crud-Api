-- Migration V10: Adicionar coluna version para controle de concorrência otimista

-- Adicionar coluna version na tabela csv_import_job
ALTER TABLE csv.csv_import_job 
ADD COLUMN version BIGINT DEFAULT 0;

-- Atualizar registros existentes para version = 0
UPDATE csv.csv_import_job 
SET version = 0 
WHERE version IS NULL;

-- Tornar a coluna NOT NULL após atualização
ALTER TABLE csv.csv_import_job 
ALTER COLUMN version SET NOT NULL;

-- Comentário explicativo
COMMENT ON COLUMN csv.csv_import_job.version IS 'Versão para controle de concorrência otimista (JPA @Version)';

-- Índice para performance nas consultas por version
CREATE INDEX IF NOT EXISTS idx_csv_import_version ON csv.csv_import_job(version); 