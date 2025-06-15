/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtParser {

  public Jwt parse(String token) {
    try {
      SignedJWT raw = SignedJWT.parse(token);
      JWTClaimsSet c = raw.getJWTClaimsSet();

      return Jwt
        .withTokenValue(token)
        .jti(c.getJWTID())
        .subject(c.getSubject())
        .issuedAt(c.getIssueTime().toInstant())
        .expiresAt(c.getExpirationTime().toInstant())
        .claims(map -> map.putAll(c.getClaims()))
        .build();
    } catch (ParseException ex) {
      throw AuthException.malformedToken();
    }
  }
}
