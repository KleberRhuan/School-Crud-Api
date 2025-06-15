/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.provider")
public record ProviderProps(@NotBlank String email) {
  public String forChannel(Channel channel) {
    return switch (channel) {
      case EMAIL -> email;
      default -> null;
    };
  }
}
