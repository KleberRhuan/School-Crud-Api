CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;

/* ---------- SCHEMAS -------------------------------------------------- */
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS config;

/* =====================================================================
   1)  AUDIT  ▸  audit.audit_event
   ===================================================================== */
-- Enum para tipo de ator
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_type WHERE typname = 'actor_type'
        ) THEN
            CREATE TYPE audit.actor_type AS ENUM ('USER', 'SYSTEM');
        END IF;
    END$$;

CREATE TABLE audit.audit_event (
                                   id          BIGSERIAL PRIMARY KEY,
                                   entity      VARCHAR(120)  NOT NULL,
                                   entity_id   TEXT,
                                   action      VARCHAR(50)   NOT NULL,
                                   ts          TIMESTAMPTZ   NOT NULL,

                                   actor_type  audit.actor_type NOT NULL DEFAULT 'USER',
                                   actor_id    BIGINT,

                                   payload     JSONB
);

CREATE INDEX idx_audit_event_entity_id
    ON audit.audit_event(entity, entity_id);

CREATE INDEX idx_audit_event_ts
    ON audit.audit_event(ts DESC);

CREATE INDEX idx_audit_event_payload_gin
    ON audit.audit_event USING gin (payload);

/* =====================================================================
   2)  OUTBOX  ▸  config.notification_outbox
   ===================================================================== */
-- Enum para canal
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_type WHERE typname = 'channel'
        ) THEN
            CREATE TYPE config.channel AS ENUM ('EMAIL', 'SMS', 'PUSH');
        END IF;
    END$$;

CREATE TABLE config.notification_outbox (
                                            id              UUID       PRIMARY KEY            DEFAULT gen_random_uuid(),
                                            recipient       CITEXT     NOT NULL,
                                            subject         TEXT       NOT NULL,
                                            body            TEXT       NOT NULL,
                                            channel         config.channel NOT NULL,

                                            next_attempt_at TIMESTAMPTZ NOT NULL,
                                            attempts        INT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_due
    ON config.notification_outbox (next_attempt_at ASC);

COMMENT ON TABLE audit.audit_event IS
    'Armazena cada INSERT/UPDATE/DELETE capturado pelo AuditEntityListener';

COMMENT ON TABLE config.notification_outbox IS
    'Pattern Outbox: fila resiliente de notificações a serem despachadas';