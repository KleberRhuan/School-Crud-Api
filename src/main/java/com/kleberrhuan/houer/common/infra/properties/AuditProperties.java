/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.audit")
public class AuditProperties {

  /** Se a auditoria está habilitada. */
  private boolean enabled = true;

  /** Se deve logar as requisições. */
  private boolean logRequests = true;

  /** Se deve logar as respostas. */
  private boolean logResponses = false;

  /** Lista de cabeçalhos sensíveis que devem ser mascarados nos logs. */
  private List<String> sensitiveHeaders = List.of(
    "Authorization",
    "Cookie",
    "X-API-Key",
    "X-Auth-Token"
  );

  /** Se deve logar o corpo das requisições. */
  private boolean logRequestBody = true;

  /** Se deve logar o corpo das respostas. */
  private boolean logResponseBody = false;

  /** Tamanho máximo do corpo da requisição/resposta para log (em bytes). */
  private int maxBodySize = 1024;

  /** Se deve logar apenas requisições que falharam. */
  private boolean logOnlyFailures = false;

  /** Lista de endpoints que devem ser ignorados na auditoria. */
  private List<String> ignoredPaths = List.of(
    "/actuator/**",
    "/swagger-ui/**",
    "/api-docs/**"
  );
}
