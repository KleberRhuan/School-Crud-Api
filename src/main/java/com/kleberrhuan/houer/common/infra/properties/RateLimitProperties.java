/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

  private boolean enabled = true;

  /** Número máximo de requisições por minuto para endpoints gerais. */
  private int requestsPerMinute = 60;

  /** Número máximo de requisições por hora para endpoints gerais. */
  private int requestsPerHour = 1000;

  /** Número máximo de requisições por minuto para endpoints de autenticação. */
  private int authRequestsPerMinute = 10;

  /** Janela de tempo em minutos para reset do contador (padrão: 1 minuto). */
  private int timeWindowMinutes = 1;

  /** Janela de tempo em horas para reset do contador de hora (padrão: 1 hora). */
  private int timeWindowHours = 1;
}
