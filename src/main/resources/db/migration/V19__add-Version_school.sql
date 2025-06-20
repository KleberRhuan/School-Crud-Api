ALTER TABLE school.school_metrics_jsonb
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;