ALTER TABLE school.school_metrics_jsonb
    DROP COLUMN IF EXISTS salas_aula,
    DROP COLUMN IF EXISTS salas_utilizadas;

DROP INDEX IF EXISTS idx_salas_aula;
DROP INDEX IF EXISTS idx_salas_utilizadas;