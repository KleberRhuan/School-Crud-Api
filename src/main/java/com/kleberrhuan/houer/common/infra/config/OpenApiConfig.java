/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Houer School Management API", description = """
    API completa para gerenciamento escolar com funcionalidades avançadas:

    ## 🎯 Funcionalidades Principais
    - **Autenticação JWT** com refresh tokens seguros
    - **Gerenciamento de Escolas** com métricas e filtros avançados
    - **Importação CSV** com processamento assíncrono
    - **Notificações em Tempo Real** via WebSocket
    - **Rate Limiting** para proteção contra abuso
    - **Observabilidade** com logs estruturados

    ## 🔌 WebSocket (Notificações em Tempo Real)
    **Endpoint:** `ws://localhost:8080/ws` (com fallback SockJS)

    **Canais Disponíveis:**
    - `/topic/csv-import/{jobId}` - Notificações de progresso de jobs específicos
    - `/user/{userId}/queue/csv-import` - Notificações privadas do usuário
    - `/user/{userId}/queue/csv-progress` - Atualizações de progresso em tempo real

    **Autenticação:** JWT via query parameter `?token=` ou header `Authorization`

    ## 🚀 Como Usar
    1. Autentique-se via `/api/v1/auth/login` para obter o token JWT
    2. Use o token em todas as requisições autenticadas
    3. Conecte-se ao WebSocket para receber notificações em tempo real
    4. Para importação CSV, monitore o progresso via WebSocket

    ## 📊 Rate Limiting
    - **Padrão:** 100 requisições por minuto por IP
    - **Login:** 10 tentativas por minuto por IP
    - **CSV Import:** 5 uploads por hora por usuário

    Para informações técnicas detalhadas, consulte os endpoints específicos abaixo.
    """, version = "1.0.0", contact = @Contact(name = "Kleber Rhuan", email = "kleber_rhuan@hotmail.com", url = "https://kleber.rhuan.cloud"), license = @License(name = "Licença Privada", url = "https://kleber.rhuan.cloud/license")), servers = {
    @Server(url = "http://localhost:8080", description = "Servidor de Desenvolvimento"),
    @Server(url = "https://api.houer.kleber.rhuan.cloud", description = "Servidor de Produção")
}, security = @SecurityRequirement(name = "BearerAuth"))
@SecurityScheme(name = "BearerAuth", description = "Autenticação JWT via Bearer Token. Obtenha seu token através do endpoint /auth/login", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@ConditionalOnProperty(name = "api.swagger.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

  @Bean
  OpenApiCustomizer globalResponses() {
    return openApi -> {
      Components c = openApi.getComponents();

      // Schemas de erro
      c.addSchemas(
          "ErrorResponse",
          new Schema<>().$ref("#/components/schemas/ErrorResponse"));

      c.addSchemas(
          "ValidationError",
          new Schema<>()
              .type("object")
              .addProperty("field", new Schema<>().type("string").description("Campo com erro"))
              .addProperty("message", new Schema<>().type("string").description("Mensagem de erro"))
              .description("Detalhes de erro de validação"));

      // Respostas padrão reutilizáveis
      c.addResponses(
          "BadRequest",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Requisição inválida - Dados mal formatados ou parâmetros inválidos")
              .content(jsonWith("ErrorResponse")));

      c.addResponses(
          "Unauthorized",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Credenciais ausentes ou inválidas - Token JWT necessário")
              .content(jsonWith("ErrorResponse")));

      c.addResponses(
          "Forbidden",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Acesso negado - Permissões insuficientes para esta operação")
              .content(jsonWith("ErrorResponse")));

      c.addResponses(
          "NotFound",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Recurso não encontrado - O item solicitado não existe")
              .content(jsonWith("ErrorResponse")));

      c.addResponses(
          "Conflict",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Conflito - Recurso já existe ou estado inválido")
              .content(jsonWith("ErrorResponse")));

      c.addResponses(
          "ValidationError",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Erro de validação - Campos obrigatórios ou formatos inválidos")
              .content(jsonWith("ErrorResponse")));

      c.addResponses(
          "InternalServerError",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Erro interno do servidor - Tente novamente em alguns instantes")
              .content(jsonWith("ErrorResponse")));

      c.addResponses(
          "TooManyRequests",
          new io.swagger.v3.oas.models.responses.ApiResponse()
              .description("Muitas requisições - Limite de taxa excedido")
              .content(jsonWith("ErrorResponse")));
    };
  }

  private static Content jsonWith(String schemaName) {
    return new Content()
        .addMediaType(
            "application/json",
            new MediaType()
                .schema(
                    new Schema<>().$ref("#/components/schemas/" + schemaName)));
  }

  /* =============================================================== */
  /* =============== Agrupamentos de documentação ================== */
  /* =============================================================== */

  @Bean
  GroupedOpenApi authApi() {
    return GroupedOpenApi
        .builder()
        .group("🔐 Autenticação")
        .displayName("Autenticação e Autorização")
        .pathsToMatch("/api/v1/auth/**")
        .build();
  }

  @Bean
  GroupedOpenApi schoolsApi() {
    return GroupedOpenApi
        .builder()
        .group("🏫 Escolas")
        .displayName("Gerenciamento de Escolas")
        .pathsToMatch("/api/v1/schools/**")
        .build();
  }

  @Bean
  GroupedOpenApi csvImportApi() {
    return GroupedOpenApi
        .builder()
        .group("📊 Importação CSV")
        .displayName("Importação e Processamento de Dados")
        .pathsToMatch("/api/csv/**", "/api/v1/csv/**")
        .build();
  }

  @Bean
  GroupedOpenApi adminApi() {
    return GroupedOpenApi
        .builder()
        .group("⚙️ Administração")
        .displayName("Funcionalidades Administrativas")
        .pathsToMatch("/api/v1/admin/**")
        .build();
  }

  @Bean
  GroupedOpenApi fullApi() {
    return GroupedOpenApi
        .builder()
        .group("📚 API Completa")
        .displayName("Todas as Funcionalidades")
        .pathsToMatch("/**")
        .build();
  }
}
