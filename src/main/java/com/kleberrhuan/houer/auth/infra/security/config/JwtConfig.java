/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.config;

import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import com.kleberrhuan.houer.common.infra.config.CacheFactory;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.validation.Valid;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.validation.annotation.Validated;

@Configuration
@RequiredArgsConstructor
@Validated
public class JwtConfig {

  @Valid
  private final JwtProps props;

  @Bean
  KeyPair keyPair() {
    return new KeyPair(props.publicKey(), props.privateKey());
  }

  @Bean
  JwtEncoder jwtEncoder(KeyPair keyPair) {
    RSAKey jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
      .privateKey((RSAPrivateKey) keyPair.getPrivate())
      .build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(
      new JWKSet(jwk)
    );
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  JwtDecoder jwtDecoder(KeyPair keyPair, CacheFactory cacheFactory) {
    var jwtBlockList = cacheFactory.getJwtBlockList();
    NimbusJwtDecoder decoder = NimbusJwtDecoder
      .withPublicKey((RSAPublicKey) keyPair.getPublic())
      .build();

    OAuth2TokenValidator<Jwt> notRevoked = jwt ->
      jwtBlockList.getIfPresent(jwt.getId()) != null
        ? OAuth2TokenValidatorResult.failure(
          new OAuth2Error("revoked", "Token is revoked", null)
        )
        : OAuth2TokenValidatorResult.success();

    decoder.setJwtValidator(
      new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefault(),
        notRevoked
      )
    );
    return decoder;
  }
}
