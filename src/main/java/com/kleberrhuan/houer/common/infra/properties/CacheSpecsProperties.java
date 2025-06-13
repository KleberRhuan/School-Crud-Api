/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "houer.caches")
public class CacheSpecsProperties {

  private Map<String, Spec> specs = new HashMap<>();

  @Getter
  @Setter
  public static class Spec {

    private Duration ttl;
    private long maxSize;
  }

  public Spec get(String name) {
    return specs.getOrDefault(name, specs.get("default"));
  }

  public Spec getDefault() {
    return specs.get("default");
  }
}
