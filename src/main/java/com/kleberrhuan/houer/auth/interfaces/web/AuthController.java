/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.web;

import com.kleberrhuan.houer.auth.application.service.AuthenticationService;
import com.kleberrhuan.houer.auth.application.service.RegistrationService;
import com.kleberrhuan.houer.auth.infra.security.jwt.TokenPair;
import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.response.TokenResponse;
import com.kleberrhuan.houer.common.interfaces.documentation.controllers.AuthControllerDocumentation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  @Value("${app.frontend-url}")
  private String frontendUrl;

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
    String baseUrl = ServletUriComponentsBuilder
      .fromRequest(req)
      .replacePath(null)
      .build()
      .toUriString();

    registration.register(dto, baseUrl);
  }

  @GetMapping("/verify")
  @ResponseStatus(HttpStatus.FOUND)
  public void verify(@RequestParam UUID token, HttpServletResponse res) {
    registration.verify(token);
    res.setHeader(HttpHeaders.LOCATION, frontendUrl + "/auth/verified");
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
  public void logout(
    @AuthenticationPrincipal JwtAuthenticationToken jwt,
    HttpServletResponse res
  ) {
    auth.logout(jwt.getToken().getId());
    clearCookie(res);
  }

  /* ======== helpers ================================================= */

  private static ResponseCookie buildRefreshCookie(String value, long ttl) {
    return ResponseCookie
      .from(COOKIE, value)
      .path("/auth/refresh")
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
        .path("/auth/refresh")
        .maxAge(0)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .build()
        .toString()
    );
  }
}
