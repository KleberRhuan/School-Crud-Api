/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.service;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.RefreshToken;
import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtParser;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenDomainService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtParser jwtParser;

  public RefreshToken findAndValidateRefreshToken(String refreshJwt) {
    Jwt parsed = jwtParser.parse(refreshJwt);
    UUID series = UUID.fromString(parsed.getId());

    RefreshToken refreshToken = refreshTokenRepository
      .findById(series)
      .orElseThrow(() -> {
        log.warn("Refresh token not found: {}", series);
        return AuthException.refreshNotFound();
      });

    validateTokenNotUsedOrExpired(refreshToken);
    return refreshToken;
  }

  public void markTokenAsUsed(RefreshToken refreshToken) {
    refreshToken.use();
    refreshTokenRepository.save(refreshToken);
  }

  private void validateTokenNotUsedOrExpired(RefreshToken refreshToken) {
    if (
      refreshToken.isUsed() ||
      refreshToken.getExpiresAt().isBefore(Instant.now())
    ) {
      log.warn(
        "Refresh token is used or expired: {}",
        refreshToken.getSeries()
      );
      throw AuthException.refreshExpired();
    }
  }
}
