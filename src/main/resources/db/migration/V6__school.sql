-- Migration V6: Criação da tabela school


CREATE SCHEMA IF NOT EXISTS school;

CREATE TABLE school.school (
  code          BIGSERIAL PRIMARY KEY,  -- Código da escola (CODESC)
  nome_dep      TEXT,                 -- Nome da rede de ensino (NOMEDEP)
  de            TEXT,                 -- Nome da diretoria de ensino (DE)
  mun           TEXT,                 -- Nome do município (MUN)
  distr         TEXT,                 -- Nome do distrito (DISTR)
  nome_esc      TEXT,                 -- Nome da escola (NOMESC)
  tipo_esc      SMALLINT,             -- Tipo da escola - numérico (TIPOESC)
  tipo_esc_desc TEXT,                 -- Tipo descrição escola (TIPOESC_DESC)
  codsit        SMALLINT,             -- Situação escola convertida para código (SITUACAO)
  codesc        bigint,              -- Código da escola original (CODESC - pode ser igual a code)
  
  -- Colunas de auditoria (SoftDeletableAuditable)
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by    BIGINT,
  updated_by    BIGINT NOT NULL,
  deleted       BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at    TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_school_mun ON school.school(mun);
CREATE INDEX idx_school_tipo_esc ON school.school(tipo_esc);
CREATE INDEX idx_school_codsit ON school.school(codsit);
CREATE INDEX idx_school_deleted ON school.school(deleted);

-- Comentários das colunas baseados no dicionário
COMMENT ON TABLE school.school IS 'Dados básicos das escolas conforme dicionário';
COMMENT ON COLUMN school.school.code IS 'Código único da escola (PK)';
COMMENT ON COLUMN school.school.nome_dep IS 'Nome da rede de ensino (NOMEDEP)';
COMMENT ON COLUMN school.school.de IS 'Nome da diretoria de ensino (DE)';
COMMENT ON COLUMN school.school.mun IS 'Nome do município (MUN)';
COMMENT ON COLUMN school.school.distr IS 'Nome do distrito (DISTR)';
COMMENT ON COLUMN school.school.nome_esc IS 'Nome da escola (NOMESC)';
COMMENT ON COLUMN school.school.tipo_esc IS 'Tipo da escola - numérico inteiro (TIPOESC)';
COMMENT ON COLUMN school.school.tipo_esc_desc IS 'Tipo descrição escola (TIPOESC_DESC)';
COMMENT ON COLUMN school.school.codsit IS 'Situação escola convertida para código (SITUACAO)';
COMMENT ON COLUMN school.school.codesc IS 'Código da escola original (CODESC)';
