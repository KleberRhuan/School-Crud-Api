-- Migration V5: Criação da tabela metric_dictionary
-- Responsável por armazenar os dicionários de métricas das escolas
CREATE SCHEMA IF NOT EXISTS school;

CREATE TABLE IF NOT EXISTS school.metric_dictionary (
  metric_code  TEXT PRIMARY KEY,
  metric_name  TEXT NOT NULL,
  data_type    TEXT NOT NULL CHECK (data_type IN ('INT'))
);

-- Comentários das colunas
COMMENT ON TABLE school.metric_dictionary IS 'Dicionário de métricas válidas para escolas';
COMMENT ON COLUMN school.metric_dictionary.metric_code IS 'Código único da métrica (ex: SALAS_AULA)';
COMMENT ON COLUMN school.metric_dictionary.metric_name IS 'Nome descritivo da métrica';
COMMENT ON COLUMN school.metric_dictionary.data_type IS 'Tipo de dado da métrica (INT para inteiro)';