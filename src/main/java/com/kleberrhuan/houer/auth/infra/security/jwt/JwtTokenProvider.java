/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import com.kleberrhuan.houer.auth.domain.model.Role;
import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import com.kleberrhuan.houer.user.domain.model.User;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtEncoder encoder;

  @Valid
  private final JwtProps props;

  public String accessToken(User u, String jti) {
    return encode(u, jti, props.accessTtlSec());
  }

  public String refreshToken(User u, UUID series) {
    return encode(u, series.toString(), props.refreshTtlSec());
  }

  private String encode(User u, String id, long ttlSec) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet
      .builder()
      .id(id)
      .subject(u.getId().toString())
      .issuer(props.issuer())
      .issuedAt(now)
      .expiresAt(now.plusSeconds(ttlSec))
      .claim(
        "roles",
        String.join(" ", u.getRoles().stream().map(Role::name).toList())
      )
      .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
