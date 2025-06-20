CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;     
CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;      
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;  
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;

/* ============== SCHEMA ===================================================== */
CREATE SCHEMA IF NOT EXISTS account;

/* ============== USERS ====================================================== */
CREATE TABLE IF NOT EXISTS account.users (
                               id            BIGSERIAL PRIMARY KEY,
                               name          VARCHAR(120) NOT NULL,
                               email         CITEXT       NOT NULL,
                               password_hash VARCHAR(255) NOT NULL,
                               enabled       BOOLEAN      NOT NULL DEFAULT FALSE,

                               deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
                               deleted_at    TIMESTAMPTZ,

                               created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                               updated_at    TIMESTAMPTZ,
                               created_by    BIGINT,
                               updated_by    BIGINT,

                               CONSTRAINT chk_users_email_format
                                   CHECK ( email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$' ),
                               CONSTRAINT uk_users_email UNIQUE (email)
);

ALTER TABLE account.users
    ADD CONSTRAINT fk_users_created_by
        FOREIGN KEY (created_by) REFERENCES account.users(id)
            ON DELETE SET NULL;

ALTER TABLE account.users
    ADD CONSTRAINT fk_users_updated_by
        FOREIGN KEY (updated_by) REFERENCES account.users(id)
            ON DELETE SET NULL;

CREATE OR REPLACE FUNCTION account.unaccent_lower(text)
    RETURNS text
    LANGUAGE sql
    IMMUTABLE
AS $$
SELECT public.unaccent(lower($1)::text);
$$;

-- índice trigram usando a função wrapper
CREATE INDEX IF NOT EXISTS idx_users_name_unaccent_ci
    ON account.users
        USING gin (account.unaccent_lower(name) gin_trgm_ops);

/* ============== USER_ROLES ================================================= */
CREATE TABLE IF NOT EXISTS account.user_roles (
                                    user_id BIGINT      NOT NULL,
                                    role    VARCHAR(50) NOT NULL,
                                    PRIMARY KEY (user_id, role),
                                    CONSTRAINT fk_user_roles_user
                                        FOREIGN KEY (user_id) REFERENCES account.users(id)
                                            ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role ON account.user_roles(role);

/* ============== Verification Tokens ================================================= */

CREATE TABLE IF NOT EXISTS account.verification_tokens (
                                                           token       UUID PRIMARY KEY,
                                                           user_id     BIGINT  NOT NULL,
                                                           expires_at  TIMESTAMPTZ NOT NULL,
                                                           used        BOOLEAN      DEFAULT false,
                                                           CONSTRAINT fk_vt_user FOREIGN KEY (user_id)
                                                               REFERENCES account.users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_vt_user_used
    ON account.verification_tokens (user_id, used);