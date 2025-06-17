/* ============== PASSWORD RESET TOKENS ================================== */
CREATE TABLE account.password_reset (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      BIGINT       NOT NULL,
    token_hash   CHAR(255)     NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    used_at      TIMESTAMPTZ  NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_password_reset_user 
        FOREIGN KEY (user_id) REFERENCES account.users(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT uk_password_reset_token_hash 
        UNIQUE (token_hash)
);

-- Índice composto para consultas de validação
CREATE INDEX idx_password_reset_user_expires 
    ON account.password_reset (user_id, expires_at);

-- Índice para busca por hash do token
CREATE INDEX idx_password_reset_token_hash 
    ON account.password_reset (token_hash);

-- Comentários para documentação
COMMENT ON TABLE account.password_reset IS 'Tokens para redefinição de senha com TTL configurável';
COMMENT ON COLUMN account.password_reset.token_hash IS 'Hash SHA-256 do token (nunca armazenar valor bruto)';
COMMENT ON COLUMN account.password_reset.expires_at IS 'Timestamp de expiração do token';
COMMENT ON COLUMN account.password_reset.used_at IS 'Timestamp de uso do token (NULL = não usado)'; 