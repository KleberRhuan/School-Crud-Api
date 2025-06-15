/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.web;

import com.kleberrhuan.houer.auth.application.service.AuthenticationService;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final String COOKIE = "REFRESH_TOKEN";
  private final AuthenticationService auth;

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
    @Valid @RequestBody LoginRequest dto,
    HttpServletResponse res
  ) {
    TokenPair pair = auth.login(dto);

    pair
      .refresh()
      .ifPresent(rt ->
        res.addHeader(
          HttpHeaders.SET_COOKIE,
          buildCookie(rt, pair.ttlSec()).toString()
        )
      );

    return ResponseEntity.ok(TokenResponse.withDefaults(pair.access()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
    @CookieValue(COOKIE) String refresh,
    HttpServletResponse res
  ) {
    TokenPair pair = auth.refresh(refresh);
    String refreshToken = pair
      .refresh()
      .orElseThrow(() -> new IllegalStateException("refresh token is null"));

    res.addHeader(
      HttpHeaders.SET_COOKIE,
      buildCookie(refreshToken, pair.ttlSec()).toString()
    );

    return ResponseEntity.ok(TokenResponse.withDefaults(pair.access()));
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
    @AuthenticationPrincipal JwtAuthenticationToken jwt,
    HttpServletResponse res
  ) {
    auth.logout(jwt.getToken().getId());

    ResponseCookie clear = ResponseCookie
      .from(COOKIE, "")
      .path("/auth/refresh")
      .httpOnly(true)
      .secure(true)
      .maxAge(0)
      .sameSite("Strict")
      .build();
    res.addHeader(HttpHeaders.SET_COOKIE, clear.toString());
  }

  /* ---------- cookie helper ---------------------------------- */
  private ResponseCookie buildCookie(String value, long ttlSec) {
    return ResponseCookie
      .from(COOKIE, value)
      .path("/auth/refresh")
      .maxAge(ttlSec)
      .httpOnly(true)
      .secure(true)
      .sameSite("Strict")
      .build();
  }
}
