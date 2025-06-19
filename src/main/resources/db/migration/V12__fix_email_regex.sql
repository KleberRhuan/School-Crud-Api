-- V12: Ajustar regex do CHECK de e-mail para aceitar hífen e caracteres comuns corretamente

ALTER TABLE account.users
  DROP CONSTRAINT IF EXISTS chk_users_email_format;

-- A classe [-] deve vir no início ou escapada; também usamos classes minúsculas já que o operador ~* é case-insensitive
ALTER TABLE account.users
  ADD CONSTRAINT chk_users_email_format
  CHECK (
    email ~* '^[a-z0-9._%+\-]+@[a-z0-9.-]+\.[a-z]{2,}$'
  ); 