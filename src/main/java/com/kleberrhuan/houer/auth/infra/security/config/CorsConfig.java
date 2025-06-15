/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.config;

import com.kleberrhuan.houer.common.infra.properties.CorsProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@Validated
public class CorsConfig {

  private final CorsProperties corsProperties;

  @Bean(name = "custom-cors")
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOriginPatterns(corsProperties.allowedOrigins());
    configuration.setAllowedMethods(corsProperties.allowedMethods());
    configuration.setAllowedHeaders(corsProperties.allowedHeaders());
    configuration.setAllowCredentials(corsProperties.allowCredentials());
    configuration.setExposedHeaders(
      List.of("Authorization", "Content-Type", "X-Password-Reset-Token")
    );
    configuration.setMaxAge(corsProperties.maxAge());

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
