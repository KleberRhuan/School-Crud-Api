/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.properties;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class AuthProperties {

  private Reset reset = new Reset();
  private Cache cache = new Cache();

  @Getter
  @Setter
  public static class Reset {

    /** Comprimento do código de verificação. Padrão: 6 */
    private int codeLength = 6;

    /** TTL em segundos para tokens de reset. Padrão: 900 (15 minutos) */
    private int ttlInSeconds = 900;

    /** Máximo de tentativas de reset por usuário por hora. Padrão: 3 */
    private int maxAttemptsPerHour = 3;

    /** URL base do frontend para links de reset */
    private String frontendBaseUrl = "http://localhost:3000";

    public Duration getTtl() {
      return Duration.ofSeconds(ttlInSeconds);
    }
  }

  @Getter
  @Setter
  public static class Cache {

    /** Provedor de cache. Padrão: caffeine */
    private String provider = "caffeine";
  }

  @PostConstruct
  void validate() {
    if (reset.ttlInSeconds < 300 || reset.ttlInSeconds > 3600) {
      throw new IllegalArgumentException(
        "Reset TTL deve estar entre 300 e 3600 segundos (5-60 minutos). Atual: " +
        reset.ttlInSeconds
      );
    }

    if (reset.maxAttemptsPerHour < 1 || reset.maxAttemptsPerHour > 10) {
      throw new IllegalArgumentException(
        "Max reset attempts deve estar entre 1 e 10. Atual: " +
        reset.maxAttemptsPerHour
      );
    }
  }
}
