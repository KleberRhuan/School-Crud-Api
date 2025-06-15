/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.error;

import com.kleberrhuan.houer.common.infra.properties.HouerProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum ApiErrorType {
  // Erros de formatação e sintaxe
  MESSAGE_NOT_READABLE("/errors/message-not-readable", "Requisição inválida"),
  RATE_LIMIT_EXCEEDED(
    "/errors/rate-limit-exceeded",
    "Limite de solicitações excedido"
  ),

  // Erros de autenticação e tokens
  INVALID_TOKEN("/errors/invalid-token", "Token inválido"),
  INVALID_FILTER_PARAM_VALUE(
    "/errors/invalid-filter-param-value",
    "O valor informado para o parâmetro de filtro é inválido."
  ),
  TOKEN_EXPIRED("/errors/token-expired", "Token expirado"),
  INSUFFICIENT_SCOPE("/errors/insufficient-scope", "Escopo insuficiente"),
  INVALID_CREDENTIALS("/errors/invalid-credentials", "Credenciais inválidas"),
  BAD_CREDENTIALS("/errors/bad-credentials", "Credenciais inválidas"),
  ACCOUNT_NOT_VERIFIED("/errors/account-not-verified", "Conta não verificada"),
  REFRESH_NOT_FOUND(
    "/errors/refresh-not-found",
    "Refresh-token nao encontrado"
  ),
  REFRESH_EXPIRED("/errors/refresh-expired", "Refresh-token expirado"),
  TOKEN_MALFORMED("/errors/malformed-token", "Token JWT mal-formado"),
  VERIFICATION_INVALID(
    "/errors/verification-invalid",
    "Token de verificação inválido"
  ),
  VERIFICATION_EXPIRED(
    "/errors/verification-expired",
    "Token de verificação expirado"
  ),

  // Erros relacionados a contas
  ACCOUNT_LOCKED("/errors/account-locked", "Conta bloqueada"),
  ACCOUNT_DISABLED("/errors/account-disabled", "Conta desabilitada"),
  CREDENTIALS_EXPIRED("/errors/credentials-expired", "Credenciais expiradas"),

  // Erros de redefinição de senha
  PASSWORD_RESET_CODE_USED(
    "/errors/password-reset-code-used",
    "Código de redefinição utilizado"
  ),
  PASSWORD_RESET_CODE_EXPIRED(
    "/errors/password-reset-code-expired",
    "Código de redefinição expirado"
  ),
  PASSWORD_RESET_CODE_INVALID(
    "/errors/password-reset-code-invalid",
    "Código de redefinição inválido"
  ),
  PASSWORD_RESET_TOKEN_INVALID(
    "/errors/password-reset-token-invalid",
    "Token de redefinição inválido"
  ),
  PASSWORD_RESET_TOKEN_EXPIRED(
    "/errors/password-reset-token-expired",
    "Token de redefinição expirado"
  ),
  PASSWORD_RESET_TOKEN_USED(
    "/errors/password-reset-token-used",
    "Token de redefinição utilizado"
  ),

  // Erros de parâmetros e busca
  INVALID_PARAMETER("/errors/invalid-parameter", "Parâmetro inválido"),
  UNAUTHORIZED("/errors/unauthorized", "Não autenticado"),
  CONFLICT("/errors/conflict", "Conflito de dados"),
  NOT_ACCEPTABLE(
    "/errors/not-acceptable",
    "O tipo de media informado nao e valido"
  ),
  UNSUPPORTED_MEDIA_TYPE(
    "/errors/unsupported-media-type",
    "O Media Type informado nao e suportado."
  ),
  INVALID_SORT_FIELD(
    "/errors/sort/invalid-field",
    "Parâmetro de ordenação inválido"
  ),
  INVALID_SEARCH_FIELD(
    "/errors/search/invalid-field",
    "Parâmetro de pesquisa inválido"
  ),

  // Erros de estado de recurso
  ALREADY_DELETED("/errors/already-deleted", "Entidade já excluída"),
  OUTBOX_PERSISTENCE_ERROR(
    "/errors/outbox-persistence-error",
    "Erro ao persistir notificação"
  ),
  OUTBOX_NOT_FOUND(
    "/errors/outbox-not-found",
    "Outbox de notificação não encontrado"
  ),
  RESOURCE_NOT_FOUND("/errors/resource-not-found", "Recurso não encontrado"),

  // Erros de negócio e validação
  CONSTRAINT_VIOLATION(
    "/errors/constraint-violation",
    "Violação de regra de negócio"
  ),
  BUSINESS_ERROR("/errors/business", "Regra de negócio violada"),

  // Erros de serviços e provedores
  INVALID_PROVIDER(
    "/errors/invalid-provider",
    "Provedor de notificação inválido"
  ),
  EMAIL_DELIVERY_ERROR("/errors/email-delivery-error", "Erro ao enviar email"),

  // Erros de sistema e infraestrutura
  SYSTEM_ERROR("/errors/system", "Erro interno"),

  // Erros de acesso e método
  FORBIDDEN("/errors/forbidden", "Acesso negado"),
  METHOD_NOT_ALLOWED("/errors/method-not-allowed", "Método não permitido");

  private final String title;
  private final String uri;

  @Setter
  private static String baseUrl;

  ApiErrorType(String uri, String title) {
    this.title = title;
    this.uri = uri;
    setBaseUrl(new HouerProperties().getUrl());
  }

  public String getUri() {
    if (baseUrl == null) {
      throw new IllegalStateException(
        "A URL base para URIs de erro não foi configurada"
      );
    }
    return baseUrl + uri;
  }
}
