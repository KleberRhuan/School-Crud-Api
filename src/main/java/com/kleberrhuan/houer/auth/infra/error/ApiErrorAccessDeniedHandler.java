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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiErrorAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper mapper;
  private final ApiErrorResponseFactory factory;

  @Override
  public void handle(
    HttpServletRequest req,
    HttpServletResponse res,
    AccessDeniedException ex
  ) {
    res.setStatus(HttpStatus.FORBIDDEN.value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);

    var body = factory.build(
      HttpStatus.FORBIDDEN,
      ApiErrorType.FORBIDDEN,
      MessageKey.of("error.business.security.forbidden"),
      req.getLocale(),
      new Object[] { ex.getMessage() }
    );

    try {
      mapper.writeValue(res.getOutputStream(), body);
    } catch (java.io.IOException ignored) {}
  }
}
