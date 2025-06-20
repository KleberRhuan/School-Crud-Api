-- Fix column type incompatibility for password_reset.token_hash
-- Change from CHAR(255) to VARCHAR(255) to match JPA entity expectation

ALTER TABLE account.password_reset 
    ALTER COLUMN token_hash TYPE VARCHAR(255);

-- Update comment to reflect the change
COMMENT ON COLUMN account.password_reset.token_hash IS 'Hash SHA-256 do token como VARCHAR(255) (nunca armazenar valor bruto)'; 