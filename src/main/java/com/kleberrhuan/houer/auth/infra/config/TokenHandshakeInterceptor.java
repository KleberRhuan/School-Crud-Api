/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.config;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.infra.security.jwt.JwtToAuthConverter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenHandshakeInterceptor implements HandshakeInterceptor {

  private final JwtDecoder jwtDecoder;
  private final JwtToAuthConverter jwtToAuthConverter;

  @Override
  public boolean beforeHandshake(
    @NotNull ServerHttpRequest request,
    @NotNull ServerHttpResponse response,
    @NotNull WebSocketHandler wsHandler,
    @NotNull Map<String, Object> attributes
  ) {
    String token = getTokenFromQuery(request);

    if (token == null) {
      token = getTokenFromHeader(request);
    }

    if (token != null && isValidToken(token)) {
      Authentication auth = validateAndCreateAuth(token);
      attributes.put("user", auth);
      log.info("WebSocket handshake successful for user: {}", auth.getName());
      return true;
    }

    log.warn("WebSocket handshake failed - invalid or missing token");
    return false;
  }

  @Override
  public void afterHandshake(
    @NotNull ServerHttpRequest request,
    @NotNull ServerHttpResponse response,
    @NotNull WebSocketHandler wsHandler,
    Exception exception
  ) {
    if (exception != null) {
      log.error("Error during WebSocket handshake", exception);
    }
  }

  private String getTokenFromQuery(ServerHttpRequest request) {
    String query = request.getURI().getQuery();
    if (query != null) {
      return extractTokenFromQuery(query);
    }
    return null;
  }

  private String getTokenFromHeader(ServerHttpRequest request) {
    String auth = request.getHeaders().getFirst("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      return auth.substring(7);
    }
    return null;
  }

  private String extractTokenFromQuery(String query) {
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      if (keyValue.length == 2 && "token".equals(keyValue[0])) {
        return keyValue[1];
      }
    }
    return null;
  }

  private boolean isValidToken(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      return (
        jwt.getExpiresAt() != null &&
        jwt.getExpiresAt().isAfter(java.time.Instant.now())
      );
    } catch (JwtException e) {
      log.debug("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  private Authentication validateAndCreateAuth(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      return jwtToAuthConverter.convert(jwt);
    } catch (JwtException e) {
      log.error("Failed to create authentication from token", e);
      throw AuthException.malformedToken();
    }
  }
}
