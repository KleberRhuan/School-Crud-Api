/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiErrorAuthenticationEntryPoint
  implements AuthenticationEntryPoint {

  private final ObjectMapper mapper;
  private final ApiErrorResponseFactory factory;

  @Override
  public void commence(
    HttpServletRequest req,
    HttpServletResponse res,
    AuthenticationException ex
  ) {
    res.setStatus(HttpStatus.UNAUTHORIZED.value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);

    var body = factory.build(
      HttpStatus.UNAUTHORIZED,
      ApiErrorType.UNAUTHORIZED,
      MessageKey.of("error.business.security.unauthenticated"),
      req.getLocale(),
      new Object[] { ex.getMessage() }
    );

    try {
      mapper.writeValue(res.getOutputStream(), body);
    } catch (java.io.IOException ignored) {}
  }
}
