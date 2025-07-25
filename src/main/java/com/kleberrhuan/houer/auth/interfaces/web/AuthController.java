/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.web;

import com.kleberrhuan.houer.auth.application.service.AuthenticationService;
import com.kleberrhuan.houer.auth.application.service.RegistrationService;
import com.kleberrhuan.houer.auth.infra.properties.AuthProperties;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ResendVerificationRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.response.TokenResponse;
import com.kleberrhuan.houer.common.interfaces.documentation.controllers.AuthControllerDocumentation;
import com.kleberrhuan.houer.user.interfaces.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocumentation {

  private static final String COOKIE = "REFRESH_TOKEN";
  private final AuthenticationService auth;
  private final RegistrationService registration;
  private final AuthProperties authProperties;

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
          buildRefreshCookie(rt, pair.ttlSec()).toString()
        )
      );

    return ResponseEntity.ok(TokenResponse.withDefaults(pair.access()));
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public void register(
    @Valid @RequestBody RegisterRequest dto,
    HttpServletRequest req
  ) {
    registration.register(dto, authProperties.getReset().getFrontendBaseUrl());
  }

  @PostMapping("/resend-verification")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void resendVerification(
    @Valid @RequestBody ResendVerificationRequest dto,
    HttpServletRequest req
  ) {
    String baseUrl = ServletUriComponentsBuilder
      .fromRequest(req)
      .replacePath(null)
      .build()
      .toUriString();

    registration.resendVerification(dto.email(), baseUrl);
  }

  @GetMapping("/verify")
  public ResponseEntity<Void> verify(@RequestParam UUID token) {
    registration.verify(token);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
    @CookieValue(COOKIE) String refresh,
    HttpServletResponse res
  ) {
    TokenPair pair = auth.refresh(refresh);

    pair
      .refresh()
      .ifPresentOrElse(
        rt ->
          res.addHeader(
            HttpHeaders.SET_COOKIE,
            buildRefreshCookie(rt, pair.ttlSec()).toString()
          ),
        () -> clearCookie(res)
      );

    return ResponseEntity.ok(TokenResponse.withDefaults(pair.access()));
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(JwtAuthenticationToken jwt, HttpServletResponse res) {
    auth.logout(jwt.getToken().getId());
    clearCookie(res);
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(JwtAuthenticationToken jwt) {
    Long sub = Long.parseLong(jwt.getToken().getSubject());
    return ResponseEntity.ok(auth.me(sub));
  }

  /* ======== helpers ================================================= */

  private static ResponseCookie buildRefreshCookie(String value, long ttl) {
    return ResponseCookie
      .from(COOKIE, value)
      .path("/api/v1/auth/refresh")
      .maxAge(ttl)
      .httpOnly(true)
      .secure(true)
      .sameSite("Strict")
      .build();
  }

  private static void clearCookie(HttpServletResponse res) {
    res.addHeader(
      HttpHeaders.SET_COOKIE,
      ResponseCookie
        .from(COOKIE, "")
        .path("/api/v1/auth/refresh")
        .maxAge(0)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .build()
        .toString()
    );
  }
}
