/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

  @Bean
  AuditorAware<Long> auditorProvider() {
    return () ->
      Optional
        .ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .filter(Authentication::isAuthenticated)
        .map(Authentication::getPrincipal)
        .filter(Jwt.class::isInstance)
        .map(Jwt.class::cast)
        .map(jwt -> {
          String sub = jwt.getClaimAsString("sub");
          if (sub != null && !sub.isBlank()) return sub;
          return jwt.getClaimAsString("userId");
        })
        .map(Long::parseLong)
        .or(Optional::empty);
  }
}
