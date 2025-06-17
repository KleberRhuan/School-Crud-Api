/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.application.port.jwt.TokenBlockList;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.RefreshToken;
import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtParser;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtTokenProvider;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import io.micrometer.core.annotation.Counted;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final UserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder encoder;
  private final JwtTokenProvider jwt;
  private final TokenBlockList blockList;
  private final JwtProps props;
  private final JwtParser fastParser;

  /* ---------- LOGIN -------------------------------------- */
  @Counted(value = "auth.login.ok")
  @Transactional
  public TokenPair login(LoginRequest req) {
    User u = users
      .findByEmailIgnoreCase(req.email())
      .orElseThrow(AuthException::badCredentials);

    if (
      !encoder.matches(req.password(), u.getPasswordHash())
    ) throw AuthException.badCredentials();

    if (!u.isEnabled()) throw AuthException.accountNotVerified();

    TokenPair pair = issueTokens(u, req.rememberMe());
    log.info("User {} logged in successfully", u.getId());
    return pair;
  }

  /* ---------- REFRESH ------------------------------------ */
  @Counted(value = "auth.refresh.ok")
  @Transactional
  public TokenPair refresh(String refreshJwt) {
    Jwt parsed = fastParser.parse(refreshJwt);
    UUID series = UUID.fromString(parsed.getId());

    RefreshToken rt = refreshTokens
      .findById(series)
      .orElseThrow(AuthException::refreshNotFound);

    if (
      rt.isUsed() || rt.getExpiresAt().isBefore(Instant.now())
    ) throw AuthException.refreshExpired();

    rt.use();
    refreshTokens.save(rt);
    User u = users.getReferenceById(rt.getUserId());

    TokenPair pair = issueTokens(u, true);
    log.info("Token refreshed for user {}", u.getId());
    return pair;
  }

  /* ---------- LOGOUT ------------------------------------- */
  @Counted(value = "auth.logout")
  public void logout(String jti) {
    blockList.block(jti);
    log.info("Logout executed - jti={}", jti);
  }

  /* ---------- HELPERS ------------------------------------ */
  private TokenPair issueTokens(User u, boolean rememberMe) {
    String jti = UUID.randomUUID().toString();
    String access = jwt.accessToken(u, jti);

    if (!rememberMe) return new TokenPair(
      access,
      Optional.empty(),
      props.accessTtlSec()
    );

    UUID series = UUID.randomUUID();
    String refresh = jwt.refreshToken(u, series);

    refreshTokens.save(
      new RefreshToken(
        series,
        u.getId(),
        Instant.now().plusSeconds(props.refreshTtlSec()),
        false
      )
    );

    return new TokenPair(access, Optional.of(refresh), props.refreshTtlSec());
  }
}
