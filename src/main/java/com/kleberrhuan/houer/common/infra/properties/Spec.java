/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.bind.DefaultValue;

public record Spec(
  @DefaultValue("30m") Duration ttl,
  @DefaultValue("1000") @Positive long maxSize
) {}
