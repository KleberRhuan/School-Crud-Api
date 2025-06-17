/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Date;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtParser {

  public Jwt parse(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw AuthException.malformedToken();
    }

    try {
      SignedJWT raw = SignedJWT.parse(token);
      JWTClaimsSet c = raw.getJWTClaimsSet();

      Jwt.Builder builder = Jwt
        .withTokenValue(token)
        .headers(headers -> headers.putAll(raw.getHeader().toJSONObject()))
        .jti(c.getJWTID())
        .subject(c.getSubject())
        .claims(map -> map.putAll(c.getClaims()));

      Date issueTime = c.getIssueTime();
      if (issueTime != null) {
        builder.issuedAt(issueTime.toInstant());
      }

      Date expirationTime = c.getExpirationTime();
      if (expirationTime != null) {
        builder.expiresAt(expirationTime.toInstant());
      }

      return builder.build();
    } catch (ParseException ex) {
      throw AuthException.malformedToken();
    }
  }
}
