/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.BaseCallback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/** Configuração personalizada do Flyway para callbacks de inicialização */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
@ConditionalOnProperty(
  name = "spring.flyway.enabled",
  havingValue = "true",
  matchIfMissing = true
)
public class FlywayConfig {

  private final Environment environment;

  @Bean
  public FlywayConfigurationCustomizer flywayConfigurationCustomizer() {
    return configuration -> {
      configuration.callbacks(new AdminUserCreationCallback());
    };
  }

  private class AdminUserCreationCallback extends BaseCallback {

    @Override
    public boolean supports(Event event, Context context) {
      return (
        Event.AFTER_MIGRATE.equals(event) ||
        Event.AFTER_MIGRATE_APPLIED.equals(event)
      );
    }

    @Override
    public void handle(Event event, Context context) {
      log.info(
        "🚀 Executando callback pós-migração: verificação de usuário admin"
      );

      try {
        DataSource dataSource = context.getConfiguration().getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String activeProfile = String.join(
          ",",
          environment.getActiveProfiles()
        );
        if (activeProfile.isEmpty()) {
          activeProfile = "default";
        }

        log.info("📊 Perfil ativo: {}", activeProfile);
        log.info("🔧 Callback SQL será executado automaticamente pelo Flyway");

        Integer adminCount = jdbcTemplate.queryForObject(
          """
                SELECT COUNT(*)
                FROM account.users u
                JOIN account.user_roles ur ON u.id = ur.user_id
                WHERE ur.role = 'ADMIN'
                AND u.deleted = FALSE
                AND u.enabled = TRUE
                """,
          Integer.class
        );

        if (adminCount != null && adminCount > 0) {
          log.info(
            "✅ Usuário(s) admin verificado(s): {} admin(s) ativo(s)",
            adminCount
          );
        } else {
          log.warn("⚠️  Nenhum usuário admin ativo encontrado após callback");
        }
      } catch (Exception e) {
        log.error(
          "❌ Erro durante callback de verificação de admin: {}",
          e.getMessage(),
          e
        );
      }
    }
  }
}
