/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    Map<String, PasswordEncoder> encoders = Map.of(
      "argon2id",
      Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
      "bcrypt",
      new BCryptPasswordEncoder()
    );
    return new DelegatingPasswordEncoder("argon2id", encoders);
  }
}
