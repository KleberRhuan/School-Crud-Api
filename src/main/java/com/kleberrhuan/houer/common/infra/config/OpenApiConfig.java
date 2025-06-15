/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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
@OpenAPIDefinition(
  info = @Info(
    title = "School API",
    version = "v1",
    contact = @Contact(
      name = "kleber Rhuan",
      email = "kleber_rhuan@hotmail.com,",
      url = "https://kleber.rhuan.cloud"
    ),
    license = @License(
      name = "Licença Privada",
      url = "https://kleber.rhuan.cloud/license"
    )
  ),
  security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
  name = "BearerAuth",
  type = SecuritySchemeType.HTTP,
  scheme = "bearer",
  bearerFormat = "JWT"
)
@ConditionalOnProperty(
  name = "api.swagger.enabled",
  havingValue = "true",
  matchIfMissing = true
)
public class OpenApiConfig {

  @Bean
  OpenApiCustomizer globalResponses() {
    return openApi -> {
      Components c = openApi.getComponents();

      c.addSchemas(
        "ErrorResponse",
        new Schema<>().$ref("#/components/schemas/ErrorResponse")
      );

      c.addResponses(
        "BadRequest",
        new io.swagger.v3.oas.models.responses.ApiResponse()
          .description("Requisição inválida")
          .content(jsonWith())
      );

      c.addResponses(
        "Unauthorized",
        new io.swagger.v3.oas.models.responses.ApiResponse()
          .description("Credenciais ausentes ou inválidas")
          .content(jsonWith())
      );

      c.addResponses(
        "NotFound",
        new io.swagger.v3.oas.models.responses.ApiResponse()
          .description("Recurso não encontrado")
          .content(jsonWith())
      );

      c.addResponses(
        "InternalServerError",
        new io.swagger.v3.oas.models.responses.ApiResponse()
          .description("Erro inesperado no servidor")
          .content(jsonWith())
      );
    };
  }

  private static Content jsonWith() {
    return new Content()
      .addMediaType(
        "application/json",
        new MediaType()
          .schema(
            new Schema<>().$ref("#/components/schemas/" + "ErrorResponse")
          )
      );
  }

  /* =============================================================== */
  /* =============== Agrupamentos de documentação ================== */
  /* =============================================================== */

  @Bean
  GroupedOpenApi authApi() {
    return GroupedOpenApi
      .builder()
      .group("Auth")
      .pathsToMatch("/auth/**")
      .build();
  }

  @Bean
  GroupedOpenApi fullApi() {
    return GroupedOpenApi
      .builder()
      .group("Api Completa")
      .pathsToMatch("/**")
      .pathsToExclude("/auth/**")
      .build();
  }
}
