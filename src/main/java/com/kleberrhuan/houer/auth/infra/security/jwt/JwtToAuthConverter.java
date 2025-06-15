/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtToAuthConverter
  implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
    Collection<SimpleGrantedAuthority> authorities = extractRoles(jwt);

    return new JwtAuthenticationToken(jwt, authorities);
  }

  private Collection<SimpleGrantedAuthority> extractRoles(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("roles");
    if (roles == null) return List.of();

    return roles
      .stream()
      .map(r -> "ROLE_" + r.toUpperCase())
      .map(SimpleGrantedAuthority::new)
      .collect(Collectors.toSet());
  }
}
