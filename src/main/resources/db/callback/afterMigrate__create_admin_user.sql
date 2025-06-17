-- Callback SQL executado após todas as migrações
-- Cria um usuário admin de teste se não existir nenhum usuário admin

DO $$
DECLARE
    admin_exists BOOLEAN := FALSE;
    admin_email VARCHAR(255) := 'admin@houer.com';
    admin_name VARCHAR(120) := 'Administrador';
    admin_password VARCHAR(255) := '$2a$10$N.zmdr9k7uOIW8B.Wneqh.XvVWnZ6m7qBjHiQkqjjFaO7s5YjjKWG'; -- admin123
    new_user_id BIGINT;
BEGIN
    -- Verifica se já existe algum usuário com role ADMIN
    SELECT EXISTS(
        SELECT 1 
        FROM account.users u 
        JOIN account.user_roles ur ON u.id = ur.user_id 
        WHERE ur.role = 'ADMIN' 
        AND u.deleted = FALSE
    ) INTO admin_exists;

    -- Se não existe admin, cria um
    IF NOT admin_exists THEN
        -- Insere o usuário admin
        INSERT INTO account.users (name, email, password_hash, enabled, deleted, created_at, updated_at)
        VALUES (admin_name, admin_email, admin_password, TRUE, FALSE, NOW(), NOW())
        RETURNING id INTO new_user_id;

        -- Adiciona a role ADMIN
        INSERT INTO account.user_roles (user_id, role)
        VALUES (new_user_id, 'ADMIN');

        RAISE NOTICE '✅ Usuário admin criado com sucesso!';
        RAISE NOTICE '📧 Email: %', admin_email;
        RAISE NOTICE '🔑 Senha: admin123';
        RAISE NOTICE '⚠️  IMPORTANTE: Altere a senha em produção!';
    ELSE
        RAISE NOTICE 'ℹ️  Usuário admin já existe no sistema.';
    END IF;
END $$; 