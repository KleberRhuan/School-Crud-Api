/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.factory;

import com.kleberrhuan.houer.auth.application.mapper.PasswordResetMapper;
import com.kleberrhuan.houer.auth.domain.model.PasswordReset;
import com.kleberrhuan.houer.auth.infra.properties.AuthProperties;
import com.kleberrhuan.houer.common.application.service.HashService;
import com.kleberrhuan.houer.user.domain.model.User;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetFactory {

  private final AuthProperties authProperties;
  private final HashService hashService;
  private final PasswordResetMapper mapper;

  public PasswordResetTokenData createForUser(User user) {
    String rawToken = hashService.generateToken();
    String tokenHash = hashService.hash(rawToken);
    Instant expiresAt = Instant.now().plus(authProperties.getReset().getTtl());

    PasswordReset passwordReset = mapper.toEntity(user, tokenHash, expiresAt);

    return new PasswordResetTokenData(passwordReset, rawToken);
  }

  public record PasswordResetTokenData(
    PasswordReset passwordReset,
    String rawToken
  ) {}
}
