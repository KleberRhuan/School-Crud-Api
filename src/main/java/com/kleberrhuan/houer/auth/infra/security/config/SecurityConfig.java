/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.kleberrhuan.houer.auth.infra.error.ApiErrorAccessDeniedHandler;
import com.kleberrhuan.houer.auth.infra.error.ApiErrorAuthenticationEntryPoint;
import com.kleberrhuan.houer.auth.infra.observability.PostValidationFilter;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtToAuthConverter;
import com.kleberrhuan.houer.common.infra.ratelimit.web.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  SecurityFilterChain api(
    HttpSecurity http,
    JwtDecoder decoder,
    JwtToAuthConverter converter,
    PostValidationFilter postFilter,
    RateLimitFilter rlFilter,
    @Qualifier("custom-cors") CorsConfigurationSource cors,
    ApiErrorAuthenticationEntryPoint entry,
    ApiErrorAccessDeniedHandler denied
  ) throws Exception {
    http
      .authorizeHttpRequests(a ->
        a
          .requestMatchers(
            "/auth/login",
            "/auth/refresh",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .oauth2ResourceServer(o2 ->
        o2
          .jwt(j -> j.decoder(decoder).jwtAuthenticationConverter(converter))
          .authenticationEntryPoint(entry)
          .accessDeniedHandler(denied)
      )
      .addFilterBefore(rlFilter, BearerTokenAuthenticationFilter.class)
      .addFilterAfter(postFilter, BearerTokenAuthenticationFilter.class)
      .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
      .headers(headers ->
        headers.crossOriginOpenerPolicy(coop ->
          coop.policy(
            CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN
          )
        )
      )
      .cors(c -> c.configurationSource(cors))
      .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
