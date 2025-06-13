/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.validation.password")
public class PasswordValidation {

  /** Comprimento mínimo da senha. */
  private int minLength = 8;

  /** Comprimento máximo da senha. */
  private int maxLength = 24;

  /** Se deve exigir pelo menos uma letra maiúscula. */
  private boolean requireUppercase = true;

  /** Se deve exigir pelo menos uma letra minúscula. */
  private boolean requireLowercase = true;

  /** Se deve exigir pelo menos um número. */
  private boolean requireNumbers = true;

  /** Se deve exigir pelo menos um caractere especial. */
  private boolean requireSpecialChars = true;

  /** Quantidade mínima de letras maiúsculas necessárias. */
  private int uppercaseCount = 1;

  /** Quantidade mínima de letras minúsculas necessárias. */
  private int lowercaseCount = 1;

  /** Quantidade mínima de números necessários. */
  private int numbersCount = 1;

  /** Quantidade mínima de caracteres especiais necessários. */
  private int specialCharsCount = 1;
}
