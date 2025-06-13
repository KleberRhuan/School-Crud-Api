/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.brevo")
public record BrevoProps(
  @NotBlank(message = "Chave de API do Brevo deve ser informada") String apiKey,
  @NotBlank(message = "Nome do sender do Brevo deve ser informado") String name,
  @NotBlank(message = "Email do sender do Brevo deve ser informado")
  @Email
  String email,
  @NotBlank(message = "URL do Brevo deve ser informada") String url,
  @Positive int connectTimeout,
  @Positive int readTimeout
) {}
