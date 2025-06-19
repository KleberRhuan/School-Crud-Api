DO $$
DECLARE
    admin_exists BOOLEAN := FALSE;
    admin_email VARCHAR(255);
    admin_name VARCHAR(120);
    admin_password VARCHAR(255);
    new_user_id BIGINT;
    environment VARCHAR(50);
BEGIN

    SELECT CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name LIKE '%test%') 
        THEN 'test'
        ELSE COALESCE(current_setting('app.environment', true), 'production')
    END INTO environment;

    IF environment = 'production' THEN
        admin_email := COALESCE(current_setting('app.admin.email', true), 'admin@houer.com');
        admin_name := COALESCE(current_setting('app.admin.name', true), 'Administrador do Sistema');
        admin_password := COALESCE(
            current_setting('app.admin.password_hash', true), 
            '{bcrypt}$2a$10$N.zmdr9k7uOIW8B.Wneqh.XvVWnZ6m7qBjHiQkqjjFaO7s5YjjKWG'
        );
    ELSE
        -- Configurações para desenvolvimento/teste
        admin_email := 'admin@houer.com';
        admin_name := 'Administrador de Desenvolvimento';
        -- Senha simples para desenvolvimento (senha: admin123)
        admin_password := '{bcrypt}$2a$10$N.zmdr9k7uOIW8B.Wneqh.XvVWnZ6m7qBjHiQkqjjFaO7s5YjjKWG';
    END IF;

    -- Verifica se já existe algum usuário com role ADMIN ativo
    SELECT EXISTS(
        SELECT 1 
        FROM account.users u 
        JOIN account.user_roles ur ON u.id = ur.user_id 
        WHERE ur.role = 'ADMIN' 
        AND u.deleted = FALSE 
        AND u.enabled = TRUE
    ) INTO admin_exists;

    -- Se não existe admin, cria um
    IF NOT admin_exists THEN
        -- Verifica se o email já existe (mesmo que deletado)
        IF EXISTS (SELECT 1 FROM account.users WHERE email = admin_email) THEN
            -- Se existe, apenas habilita e adiciona role ADMIN se necessário
            UPDATE account.users 
            SET 
                enabled = TRUE,
                deleted = FALSE,
                deleted_at = NULL,
                name = admin_name,
                password_hash = admin_password,
                updated_at = NOW()
            WHERE email = admin_email
            RETURNING id INTO new_user_id;

            -- Adiciona role ADMIN se não existir
            INSERT INTO account.user_roles (user_id, role)
            SELECT new_user_id, 'ADMIN'
            WHERE NOT EXISTS (
                SELECT 1 FROM account.user_roles 
                WHERE user_id = new_user_id AND role = 'ADMIN'
            );

            RAISE NOTICE '🔄 Usuário admin reativado e atualizado!';
        ELSE
            -- Cria novo usuário admin
            INSERT INTO account.users (
                name, 
                email, 
                password_hash, 
                enabled, 
                deleted, 
                created_at, 
                updated_at
            )
            VALUES (
                admin_name, 
                admin_email, 
                admin_password, 
                TRUE, 
                FALSE, 
                NOW(), 
                NOW()
            )
            RETURNING id INTO new_user_id;

            -- Adiciona a role ADMIN
            INSERT INTO account.user_roles (user_id, role)
            VALUES (new_user_id, 'ADMIN');

            RAISE NOTICE '✅ Usuário admin criado com sucesso!';
        END IF;

        -- Logs informativos
        RAISE NOTICE '=================================================';
        RAISE NOTICE '🏢 Ambiente: %', UPPER(environment);
        RAISE NOTICE '📧 Email: %', admin_email;
        RAISE NOTICE '👤 Nome: %', admin_name;
        
        IF environment = 'production' THEN
            RAISE NOTICE '🔐 Use a senha configurada nas variáveis de ambiente';
            RAISE NOTICE '⚠️  IMPORTANTE: Configure app.admin.password_hash para produção!';
        ELSE
            RAISE NOTICE '🔑 Senha: admin123';
            RAISE NOTICE '⚠️  IMPORTANTE: Esta é uma senha de desenvolvimento!';
        END IF;
        
        RAISE NOTICE '=================================================';
    ELSE
        RAISE NOTICE 'ℹ️  Usuário admin já existe no sistema.';
        
        -- Exibe informações do admin existente
        SELECT u.email, u.name 
        INTO admin_email, admin_name
        FROM account.users u 
        JOIN account.user_roles ur ON u.id = ur.user_id 
        WHERE ur.role = 'ADMIN' 
        AND u.deleted = FALSE 
        AND u.enabled = TRUE
        LIMIT 1;
        
        RAISE NOTICE '📧 Admin ativo: % (%)', admin_name, admin_email;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        RAISE WARNING 'Erro ao criar usuário admin: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE '❌ Falha na criação do usuário admin. Verifique os logs.';
END $$; 