/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProps(
  @NotBlank String issuer,
  @Positive long accessTtlSec,
  @Positive long refreshTtlSec,
  @NotNull RSAPublicKey publicKey,
  @NotNull RSAPrivateKey privateKey
) {}
