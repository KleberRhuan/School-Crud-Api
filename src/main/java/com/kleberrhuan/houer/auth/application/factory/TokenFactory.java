/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.factory;

import com.kleberrhuan.houer.auth.domain.model.RefreshToken;
import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtTokenProvider;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.user.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenFactory {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProps jwtProps;

  public TokenPair createTokenPair(User user, boolean rememberMe) {
    String jti = UUID.randomUUID().toString();
    String accessToken = jwtTokenProvider.accessToken(user, jti);

    if (!rememberMe) {
      return new TokenPair(
        accessToken,
        Optional.empty(),
        jwtProps.accessTtlSec()
      );
    }

    UUID series = UUID.randomUUID();
    String refreshToken = jwtTokenProvider.refreshToken(user, series);

    RefreshToken refreshTokenEntity = new RefreshToken(
      series,
      user.getId(),
      Instant.now().plusSeconds(jwtProps.refreshTtlSec()),
      false
    );

    refreshTokenRepository.save(refreshTokenEntity);

    return new TokenPair(
      accessToken,
      Optional.of(refreshToken),
      jwtProps.refreshTtlSec()
    );
  }
}
