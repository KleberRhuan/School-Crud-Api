/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
  @NotBlank(message = "A lista de origens permitidas para CORS é obrigatória")
  List<String> allowedOrigins,
  @NotBlank(message = "A lista de métodos HTTP permitidos é obrigatória")
  List<String> allowedMethods,
  @NotBlank(message = "A lista de cabeçalhos permitidos é obrigatória")
  List<String> allowedHeaders,
  @NotNull(
    message = "O tempo de cache para preflight requests (em segundos) é obrigatório"
  )
  long maxAge,
  @NotNull(
    message = "O permitir credenciais nas requisições CORS é obrigatório"
  )
  boolean allowCredentials
) {}
