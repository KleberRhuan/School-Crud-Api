/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@EnableTransactionManagement
public class TestSecurityConfig {

  @Bean
  @Primary
  public PasswordEncoder testPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Primary
  public SecurityFilterChain testFilterChain(HttpSecurity http)
    throws Exception {
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
      .build();
  }

  @Bean
  @Primary
  public AuthenticationManager testAuthenticationManager(
    AuthenticationConfiguration authConfig
  ) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
