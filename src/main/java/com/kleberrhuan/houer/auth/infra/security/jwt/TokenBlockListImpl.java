/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.kleberrhuan.houer.auth.application.port.jwt.TokenBlockList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TokenBlockListImpl implements TokenBlockList {

  private final Cache<String, Boolean> cache;

  public TokenBlockListImpl(
    @Qualifier("jwtBlockCache") Cache<String, Boolean> cache
  ) {
    this.cache = cache;
  }

  public void block(String jti) {
    cache.put(jti, Boolean.TRUE);
  }

  public boolean isBlocked(String jti) {
    return cache.getIfPresent(jti) != null;
  }
}
