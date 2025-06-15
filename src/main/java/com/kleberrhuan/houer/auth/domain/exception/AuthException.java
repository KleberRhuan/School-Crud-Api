/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.exception;

import com.kleberrhuan.houer.common.infra.exception.ApiException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public final class AuthException extends ApiException {

  private AuthException(
    HttpStatus status,
    ApiErrorType type,
    MessageKey key,
    Object... args
  ) {
    super(status, type, key, args);
  }

  /** Credenciais inválidas (e-mail ou senha). */
  public static AuthException badCredentials() {
    return new AuthException(
      HttpStatus.UNAUTHORIZED,
      ApiErrorType.BAD_CREDENTIALS, // defina no enum
      MessageKey.of("error.auth.bad-credentials")
    );
  }

  /** Refresh-token não encontrado no banco. */
  public static AuthException refreshNotFound() {
    return new AuthException(
      HttpStatus.UNAUTHORIZED,
      ApiErrorType.REFRESH_NOT_FOUND,
      MessageKey.of("error.auth.refresh.not-found")
    );
  }

  /** Refresh-token expirado ou reutilizado. */
  public static AuthException refreshExpired() {
    return new AuthException(
      HttpStatus.GONE,
      ApiErrorType.REFRESH_EXPIRED,
      MessageKey.of("error.auth.refresh.expired")
    );
  }

  /** Token JWT mal-formado (parse falhou). */
  public static AuthException malformedToken() {
    return new AuthException(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.TOKEN_MALFORMED,
      MessageKey.of("error.auth.token.malformed")
    );
  }

  /** Conta existe, mas ainda não foi validada pelo usuário. */
  public static AuthException accountNotVerified() {
    return new AuthException(
      HttpStatus.PRECONDITION_REQUIRED,
      ApiErrorType.ACCOUNT_NOT_VERIFIED,
      MessageKey.of("error.auth.account.not-verified")
    );
  }

  /** Token de verificação não encontrado ou já utilizado. */
  public static AuthException verificationInvalid() {
    return new AuthException(
      HttpStatus.GONE,
      ApiErrorType.VERIFICATION_INVALID,
      MessageKey.of("error.auth.verification.invalid")
    );
  }

  /** Token de verificação expirou antes do clique. */
  public static AuthException verificationExpired() {
    return new AuthException(
      HttpStatus.GONE,
      ApiErrorType.VERIFICATION_EXPIRED,
      MessageKey.of("error.auth.verification.expired")
    );
  }
}
