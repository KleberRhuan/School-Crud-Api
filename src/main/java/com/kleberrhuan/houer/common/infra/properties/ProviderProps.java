/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.provider")
public record ProviderProps(@NotBlank String email) {}
